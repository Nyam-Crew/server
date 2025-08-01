package com.nyam.everyday.module.member.service;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.member.dto.MemberDto;
import com.nyam.everyday.web.member.mapper.MemberMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberMapper memberMapper;


  public MemberDto getMemberById(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다: id=" + id));
    return memberMapper.toDto(member);
  }

  @Transactional
  public MemberDto create(MemberDto dto) {
    Member entity = memberMapper.toEntity(dto);
    Member saved = memberRepository.save(entity);
    return memberMapper.toDto(saved);
  }

  @Transactional
  public MemberDto update(Long id, MemberDto dto) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("회원 없음"));
    memberMapper.modify(dto, member); // 필드 변경만

    return memberMapper.toDto(member); // save() 없이도 반영됨
  }

}
