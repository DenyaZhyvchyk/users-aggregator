package com.struts.rest;

import com.struts.model.AggregationResult;
import com.struts.model.User;
import com.struts.service.UserAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Users", description = "Operations for user aggregation")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserAggregationService userAggregationService;

    @Operation(
            summary = "Get aggregated list of users",
            description = "Aggregates users from multiple data sources and returns the combined list.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "All data sources responded successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = User.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "206",
                            description = "Partial data — some sources failed or were unavailable",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = User.class))
                            )
                    )
            }
    )
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
