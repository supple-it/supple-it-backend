package com.suppleit.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suppleit.backend.dto.ScheduleDto;
import com.suppleit.backend.mapper.MemberMapper;
import com.suppleit.backend.model.Member;
import com.suppleit.backend.service.ScheduleService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController extends JwtSupportController {
    private final ScheduleService scheduleService;
    private final MemberMapper memberMapper;

    @PostMapping
    public ResponseEntity<?> createSchedule(
        @RequestBody ScheduleDto scheduleDto, 
        HttpServletRequest request
    ) {
        try {
            String email = extractEmailFromToken(request);
            
            Member member = memberMapper.getMemberByEmail(email);
            scheduleDto.setMemberId(member.getMemberId());
            
            ScheduleDto createdSchedule = scheduleService.createSchedule(scheduleDto);
            return ResponseEntity.ok(createdSchedule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
    
    @GetMapping("/time/{intakeTime}")
    public ResponseEntity<?> getSchedulesByTime(
        @PathVariable String intakeTime, 
        HttpServletRequest request
    ) {
        String email = extractEmailFromToken(request);
        Member member = memberMapper.getMemberByEmail(email);
        List<ScheduleDto> schedules = scheduleService.getSchedulesByTime(member.getMemberId(), intakeTime);
        return ResponseEntity.ok(schedules);
    }
}
