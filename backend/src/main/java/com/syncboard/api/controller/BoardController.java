// Stage 2 — REST API controllers
package com.syncboard.api.controller;

import com.syncboard.api.dto.BoardResponse;
import com.syncboard.service.BoardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contract 01-CONTRACT.md §4: {@code GET /api/v1/board -> BoardResponse}. No logic here —
 * delegates to {@link BoardService}.
 */
@RestController
@RequestMapping("/api/v1")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/board")
    public BoardResponse getBoard() {
        return boardService.getBoard();
    }
}
