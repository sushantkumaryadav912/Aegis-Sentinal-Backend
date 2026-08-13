package com.aegis.identity.api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aegis/v1/rbac-test")
public class RbacTestController {

    @GetMapping("/alert-read")
    @PreAuthorize("hasAuthority('alert:read')")
    public String alertRead() {
        return "alert:read permission granted";
    }

    @GetMapping("/workflow-execute")
    @PreAuthorize("hasAuthority('workflow:execute')")
    public String workflowExecute() {
        return "workflow:execute permission granted";
    }

    @GetMapping("/org-admin")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public String orgAdmin() {
        return "ORG_ADMIN role granted";
    }

    @GetMapping("/unassigned-permission")
    @PreAuthorize("hasAuthority('system:super-admin')")
    public String unassignedPermission() {
        return "system:super-admin permission granted";
    }
}
