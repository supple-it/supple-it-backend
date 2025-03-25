package com.suppleit.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.suppleit.backend.dto.ScheduleDto;

@Mapper
public interface ScheduleMapper {
    void insertSchedule(ScheduleDto scheduleDto);
    List<ScheduleDto> getSchedulesByMemberId(Long memberId);
    List<ScheduleDto> getSchedulesByMemberIdAndTime(Long memberId, String intakeTime);
    void updateSchedule(ScheduleDto scheduleDto);
    void deleteSchedule(Long scheduleId);
}
