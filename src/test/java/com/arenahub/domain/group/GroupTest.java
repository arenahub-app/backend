package com.arenahub.domain.group;

import com.arenahub.domain.group.vo.GroupName;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.group.vo.Sport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GroupTest {

    @Test
    void create_setsActiveStatusAndGeneratesId() {
        Group group = Group.create(new GroupName("Arena FC"), Sport.FOOTBALL, "Descrição");

        assertThat(group.getId()).isNotNull();
        assertThat(group.getName().value()).isEqualTo("Arena FC");
        assertThat(group.getSport()).isEqualTo(Sport.FOOTBALL);
        assertThat(group.getStatus()).isEqualTo(GroupStatus.ACTIVE);
        assertThat(group.isActive()).isTrue();
        assertThat(group.getCreatedAt()).isNotNull();
    }

    @Test
    void create_withNullDescription_isAllowed() {
        Group group = Group.create(new GroupName("Arena FC"), Sport.FOOTBALL, null);
        assertThat(group.getDescription()).isNull();
    }

    @Test
    void update_changesOnlyProvidedFields() {
        Group group = Group.create(new GroupName("Arena FC"), Sport.FOOTBALL, "Desc");

        group.update(new GroupName("Arena SC"), null, null, "pix@key.com", null);

        assertThat(group.getName().value()).isEqualTo("Arena SC");
        assertThat(group.getSport()).isEqualTo(Sport.FOOTBALL);
        assertThat(group.getDescription()).isEqualTo("Desc");
        assertThat(group.getPixKey()).isEqualTo("pix@key.com");
    }

    @Test
    void deactivate_setsInactiveStatus() {
        Group group = Group.create(new GroupName("Arena FC"), Sport.FOOTBALL, null);

        group.deactivate();

        assertThat(group.getStatus()).isEqualTo(GroupStatus.INACTIVE);
        assertThat(group.isActive()).isFalse();
    }
}
