package com.suppleit.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.suppleit.backend.dto.ScheduleDto;
import com.suppleit.backend.mapper.ScheduleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleMapper scheduleMapper;
    
    // 스케줄 생성 (유효성 검사 포함)
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        // 시작일 유효성 검사 (현재 이후)
        validateStartDate(scheduleDto.getIntakeStart());
        
        // 복용 종료일 계산
        calculateIntakeEndDate(scheduleDto);
        
        scheduleMapper.insertSchedule(scheduleDto);
        return scheduleDto;
    }
    
    // 시작일 유효성 검사
    private void validateStartDate(LocalDate startDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("시작일은 현재 날짜 이후여야 합니다.");
        }
    }
    
    // 복용 종료일 계산
    private void calculateIntakeEndDate(ScheduleDto scheduleDto) {
        if (scheduleDto.getIntakeEnd() == null && scheduleDto.getIntakeDistance() != null) {
            scheduleDto.setIntakeEnd(
                scheduleDto.getIntakeStart().plusDays(scheduleDto.getIntakeDistance())
            );
        }
    }
    
    // 시간대별 스케줄 조회
    public List<ScheduleDto> getSchedulesByTime(Long memberId, String intakeTime) {
        return scheduleMapper.getSchedulesByMemberIdAndTime(memberId, intakeTime);
    }
}