package com.struts.rest;

import com.struts.model.AggregationResult;
import com.struts.model.User;
import com.struts.service.UserAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserAggregationService userAggregationService;
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<User>> getUsers() {
        AggregationResult result = userAggregationService.getUsers();
        if (result.partial()) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(result.users());
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(result.users());
        }
    }
}
