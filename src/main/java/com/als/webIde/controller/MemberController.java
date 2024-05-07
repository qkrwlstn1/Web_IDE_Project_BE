package com.als.webIde.controller;

import com.als.webIde.DTO.request.UserId;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.repository.MemberRepositpory;
import com.als.webIde.domain.repository.MemberSettingRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user")
public class MemberController {

    private final MemberRepositpory memberRepositpory;

    @GetMapping("/idcheck")
    public ResponseEntity<String> checkedUserId(@RequestBody UserId userId){
        List<Member> memberByUserId = memberRepositpory.findMemberByUserId(userId.getUserId());
        if (memberByUserId.isEmpty()){
            return ResponseEntity.ok("사용할 수 있는 아이디입니다.");
        }
        return ResponseEntity.status(400).body("사용할 수 없는 아이디입니다.");
    }

}
