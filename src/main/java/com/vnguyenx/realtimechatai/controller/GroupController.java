package com.vnguyenx.realtimechatai.controller;

import com.vnguyenx.realtimechatai.dto.conversation.*;
import com.vnguyenx.realtimechatai.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            Authentication authentication,
            @Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = groupService.createGroup(authentication.getName(), request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{conversationId}/name")
    public ResponseEntity<GroupResponse> renameGroup(
            Authentication authentication,
            @PathVariable Long conversationId,
            @Valid @RequestBody RenameGroupRequest request) {
        return ResponseEntity.ok(groupService.renameGroup(authentication.getName(), conversationId, request));
    }

    @PostMapping("/{conversationId}/members")
    public ResponseEntity<GroupResponse> addMember(
            Authentication authentication,
            @PathVariable Long conversationId,
            @Valid @RequestBody AddGroupMemberRequest request) {
        return ResponseEntity.ok(groupService.addMember(authentication.getName(), conversationId, request));
    }

    @DeleteMapping("/{conversationId}/members/{username}")
    public ResponseEntity<GroupResponse> kickMember(
            Authentication authentication,
            @PathVariable Long conversationId,
            @PathVariable String username) {
        return ResponseEntity.ok(groupService.kickMember(authentication.getName(), conversationId, username));
    }

    @PostMapping("/{conversationId}/leave")
    public ResponseEntity<Void> leaveGroup(
            Authentication authentication,
            @PathVariable Long conversationId) {
        groupService.leaveGroup(authentication.getName(), conversationId);
        return ResponseEntity.ok().build();
    }
}