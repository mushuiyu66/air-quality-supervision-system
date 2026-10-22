package com.hd.nepm.controller;

import com.hd.nepm.po.Supervisor;
import com.hd.nepm.service.impl.SupervisorServiceI;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Package: com.hd.nepm.controller
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 15:58
 * @Version 1.0
 */
@RestController
@RequestMapping("/supervisor")
@CrossOrigin
public class SupervisorController {
    @Autowired
    private SupervisorServiceI supervisorServicei;
    @PostMapping("/SupervisorLogin")
    public ResponseData supervisorLogin(@RequestBody Supervisor supervisor) {
        return supervisorServicei.selectByTelIdAndPassword(supervisor);
    }
    @PostMapping("/signUp")
    public ResponseData signUp(@RequestBody Supervisor supervisor) {
        return supervisorServicei.signUp(supervisor);
    }

    @GetMapping
    public ResponseData<List<Supervisor>> getAllSupervisors() {
        return supervisorServicei.getAllSupervisors();
    }

    @GetMapping("/{telId}")
    public ResponseData<Supervisor> getSupervisorByTelId(@PathVariable String telId) {
        return supervisorServicei.getSupervisorByTelId(telId);
    }

    @PostMapping("/update")
    public ResponseData<Supervisor> updateSupervisor(@RequestBody Supervisor supervisor) {
        return supervisorServicei.updateSupervisor(supervisor);
    }

    @PostMapping("/delete")
    public ResponseData<Supervisor> deleteSupervisor(@RequestBody Supervisor supervisor) {
        return supervisorServicei.deleteSupervisor(supervisor.getTelId());
    }
}
