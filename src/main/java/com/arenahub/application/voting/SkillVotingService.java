package com.arenahub.application.voting;

import com.arenahub.application.exception.*;
import com.arenahub.application.voting.port.in.*;
import com.arenahub.application.voting.port.out.GroupMemberSkillPort;
import com.arenahub.application.voting.port.out.GroupMemberSkillPort.GroupMemberView;
import com.arenahub.application.voting.port.out.SkillVotingRepository;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.Skill;
import com.arenahub.domain.group.vo.SkillSource;
import com.arenahub.domain.voting.SkillVoting;
import com.arenahub.domain.voting.Vote;
import com.arenahub.domain.voting.VotingBan;
import com.arenahub.domain.voting.service.SkillCalculationService;
import com.arenahub.domain.voting.vo.Stars;
import com.arenahub.presentation.voting.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SkillVotingService implements
        OpenVotingUseCase,
        GetActiveVotingUseCase,
        ListVotingsUseCase,
        CloseVotingUseCase,
        CastVoteUseCase,
        GetMyVotesUseCase,
        GetVotingResultsUseCase,
        BanFromVotingUseCase,
        UnbanFromVotingUseCase {

    private final SkillVotingRepository votingRepo;
    private final GroupMemberSkillPort memberPort;
    private final SkillCalculationService calculationService;

    public SkillVotingService(SkillVotingRepository votingRepo,
                               GroupMemberSkillPort memberPort,
                               SkillCalculationService calculationService) {
        this.votingRepo = votingRepo;
        this.memberPort = memberPort;
        this.calculationService = calculationService;
    }

    @Override
    public VotingResponse execute(OpenVotingUseCase.Command cmd) {
        GroupMemberView caller = requireMember(cmd.groupId(), cmd.userId());
        requireAdminOrOwner(caller);

        if (votingRepo.findActiveByGroupId(cmd.groupId()).isPresent()) {
            throw new VotingAlreadyOpenException();
        }

        SkillVoting voting = SkillVoting.open(cmd.groupId(), cmd.deadline(), cmd.userId());
        voting = votingRepo.save(voting);

        List<GroupMemberView> votableMembers = memberPort.findVotableMembers(cmd.groupId());
        int totalVotable = countVotableForCaller(votableMembers, caller.id());

        return toVotingResponse(voting, totalVotable, 0, false);
    }

    @Override
    @Transactional(readOnly = true)
    public VotingResponse execute(GetActiveVotingUseCase.Command cmd) {
        GroupMemberView caller = requireMember(cmd.groupId(), cmd.userId());

        SkillVoting voting = votingRepo.findActiveByGroupId(cmd.groupId())
                .orElseThrow(VotingNotFoundException::new);

        List<GroupMemberView> votableMembers = memberPort.findVotableMembers(cmd.groupId());
        int totalVotable = countVotableForCaller(votableMembers, caller.id());

        long myVoteCount = votingRepo.findVotesByVotingIdAndVoterId(voting.getId(), caller.id()).size();
        boolean isBanned = votingRepo.findBan(voting.getId(), caller.id()).isPresent();

        return toVotingResponse(voting, totalVotable, (int) myVoteCount, isBanned);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VotingSummaryResponse> execute(ListVotingsUseCase.Command cmd) {
        requireMember(cmd.groupId(), cmd.userId());

        return votingRepo.findByGroupId(cmd.groupId()).stream()
                .map(v -> new VotingSummaryResponse(v.getId(), v.getStatus(),
                        v.getOpenedAt(), v.getDeadline(), v.getClosedAt()))
                .toList();
    }

    @Override
    public VotingResultsResponse execute(CloseVotingUseCase.Command cmd) {
        GroupMemberView caller = requireMember(cmd.groupId(), cmd.userId());
        requireAdminOrOwner(caller);

        SkillVoting voting = requireVotingInGroup(cmd.votingId(), cmd.groupId());
        if (!voting.isOpen()) {
            throw new VotingAlreadyClosedException();
        }

        List<GroupMemberView> members = memberPort.findVotableMembers(cmd.groupId());
        Map<UUID, BigDecimal> previousSkills = members.stream()
                .collect(Collectors.toMap(GroupMemberView::id, GroupMemberView::currentSkill));

        voting.close(Instant.now());
        votingRepo.save(voting);

        Map<UUID, BigDecimal> newSkills = calculationService.calculateAndUpdate(
                voting, votingRepo, memberPort);

        return buildResultsResponse(voting, members, previousSkills, newSkills);
    }

    @Override
    public VoteResponse execute(CastVoteUseCase.Command cmd) {
        GroupMemberView caller = requireMember(cmd.groupId(), cmd.userId());

        if (caller.role() == GroupRole.REFEREE) {
            throw new InsufficientRoleException();
        }

        SkillVoting voting = requireVotingInGroup(cmd.votingId(), cmd.groupId());
        if (!voting.isOpen()) {
            throw new VotingClosedException();
        }

        if (votingRepo.findBan(voting.getId(), caller.id()).isPresent()) {
            throw new VoterIsBannedException();
        }

        GroupMemberView target = memberPort.findMemberById(cmd.targetMemberId())
                .orElseThrow(MemberNotFoundException::new);

        if (target.id().equals(caller.id())) {
            throw new SelfVoteNotAllowedException();
        }

        if (target.role() == GroupRole.REFEREE) {
            throw new MemberNotFoundException();
        }

        Stars stars = new Stars(cmd.stars());

        Vote vote = votingRepo.findVote(voting.getId(), caller.id(), target.id())
                .map(existing -> {
                    existing.update(stars);
                    return existing;
                })
                .orElse(Vote.cast(voting.getId(), cmd.groupId(), caller.id(), target.id(), stars));

        vote = votingRepo.saveVote(vote);

        return new VoteResponse(target.id(), target.userName(), vote.getStars().value(), vote.getVotedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public MyVotesResponse execute(GetMyVotesUseCase.Command cmd) {
        GroupMemberView caller = requireMember(cmd.groupId(), cmd.userId());

        if (caller.role() == GroupRole.REFEREE) {
            throw new InsufficientRoleException();
        }

        SkillVoting voting = requireVotingInGroup(cmd.votingId(), cmd.groupId());

        List<GroupMemberView> votableMembers = memberPort.findVotableMembers(cmd.groupId());
        int totalVotable = countVotableForCaller(votableMembers, caller.id());

        Map<UUID, GroupMemberView> memberById = votableMembers.stream()
                .collect(Collectors.toMap(GroupMemberView::id, m -> m));

        List<VoteResponse> myVotes = votingRepo.findVotesByVotingIdAndVoterId(voting.getId(), caller.id())
                .stream()
                .map(v -> {
                    GroupMemberView target = memberById.get(v.getTargetMemberId());
                    String name = target != null ? target.userName() : null;
                    return new VoteResponse(v.getTargetMemberId(), name,
                            v.getStars().value(), v.getVotedAt());
                })
                .toList();

        return new MyVotesResponse(voting.getId(), totalVotable, myVotes);
    }

    @Override
    @Transactional(readOnly = true)
    public VotingResultsResponse execute(GetVotingResultsUseCase.Command cmd) {
        GroupMemberView caller = requireMember(cmd.groupId(), cmd.userId());

        SkillVoting voting = requireVotingInGroup(cmd.votingId(), cmd.groupId());

        if (voting.isOpen()) {
            boolean isAdminOrOwner = caller.role() == GroupRole.ADMIN || caller.role() == GroupRole.OWNER;
            if (!isAdminOrOwner) {
                throw new ResultsNotAvailableException();
            }
        }

        List<GroupMemberView> members = memberPort.findVotableMembers(cmd.groupId());
        List<Vote> votes = votingRepo.findVotesByVotingId(voting.getId());
        List<VotingBan> bans = votingRepo.findBansByVotingId(voting.getId());

        Set<UUID> bannedVoterIds = bans.stream()
                .map(VotingBan::getMemberId)
                .collect(Collectors.toSet());

        Map<UUID, List<Integer>> votesByTarget = votes.stream()
                .filter(v -> !bannedVoterIds.contains(v.getVoterId()))
                .collect(Collectors.groupingBy(
                        Vote::getTargetMemberId,
                        Collectors.mapping(v -> v.getStars().value(), Collectors.toList())
                ));

        List<MemberResultResponse> memberResults = members.stream()
                .map(m -> {
                    List<Integer> memberVotes = votesByTarget.getOrDefault(m.id(), List.of());
                    BigDecimal avgSkill = memberVotes.isEmpty() ? null :
                            BigDecimal.valueOf(memberVotes.stream().mapToInt(i -> i).average().orElse(0))
                                    .setScale(1, RoundingMode.HALF_UP);
                    return new MemberResultResponse(m.id(), m.userName(), m.photoUrl(),
                            null, avgSkill, memberVotes.size());
                })
                .toList();

        return new VotingResultsResponse(voting.getId(), voting.getStatus(),
                voting.getClosedAt(), memberResults);
    }

    @Override
    public void execute(BanFromVotingUseCase.Command cmd) {
        GroupMemberView caller = requireMember(cmd.groupId(), cmd.userId());
        requireAdminOrOwner(caller);

        SkillVoting voting = requireVotingInGroup(cmd.votingId(), cmd.groupId());
        if (!voting.isOpen()) {
            throw new VotingClosedException();
        }

        memberPort.findMemberById(cmd.memberId())
                .orElseThrow(MemberNotFoundException::new);

        if (votingRepo.findBan(voting.getId(), cmd.memberId()).isPresent()) {
            throw new MemberAlreadyBannedFromVotingException();
        }

        VotingBan ban = VotingBan.ban(voting.getId(), cmd.groupId(),
                cmd.memberId(), cmd.reason(), cmd.userId());
        votingRepo.saveBan(ban);
    }

    @Override
    public void execute(UnbanFromVotingUseCase.Command cmd) {
        GroupMemberView caller = requireMember(cmd.groupId(), cmd.userId());
        requireAdminOrOwner(caller);

        SkillVoting voting = requireVotingInGroup(cmd.votingId(), cmd.groupId());
        if (!voting.isOpen()) {
            throw new VotingClosedException();
        }

        VotingBan ban = votingRepo.findBan(voting.getId(), cmd.memberId())
                .orElseThrow(VotingBanNotFoundException::new);

        votingRepo.deleteBan(ban.getId());
    }

    private GroupMemberView requireMember(UUID groupId, UUID userId) {
        return memberPort.findMemberByGroupAndUser(groupId, userId)
                .orElseThrow(NotAMemberException::new);
    }

    private SkillVoting requireVotingInGroup(UUID votingId, UUID groupId) {
        return votingRepo.findByIdAndGroupId(votingId, groupId)
                .orElseThrow(VotingNotFoundException::new);
    }

    private void requireAdminOrOwner(GroupMemberView member) {
        if (member.role() != GroupRole.ADMIN && member.role() != GroupRole.OWNER) {
            throw new InsufficientRoleException();
        }
    }

    private int countVotableForCaller(List<GroupMemberView> allVotable, UUID callerId) {
        return (int) allVotable.stream()
                .filter(m -> !m.id().equals(callerId))
                .count();
    }

    private VotingResponse toVotingResponse(SkillVoting voting, int totalVotable,
                                             int myVoteCount, boolean isBanned) {
        return new VotingResponse(voting.getId(), voting.getGroupId(), voting.getStatus(),
                voting.getOpenedBy(), voting.getOpenedAt(), voting.getDeadline(),
                voting.getClosedAt(), totalVotable, myVoteCount, isBanned);
    }

    private VotingResultsResponse buildResultsResponse(SkillVoting voting,
                                                        List<GroupMemberView> members,
                                                        Map<UUID, BigDecimal> previousSkills,
                                                        Map<UUID, BigDecimal> newSkills) {
        List<MemberResultResponse> results = members.stream()
                .map(m -> new MemberResultResponse(
                        m.id(),
                        m.userName(),
                        m.photoUrl(),
                        previousSkills.get(m.id()),
                        newSkills.get(m.id()),
                        0
                ))
                .toList();

        return new VotingResultsResponse(voting.getId(), voting.getStatus(),
                voting.getClosedAt(), results);
    }
}
