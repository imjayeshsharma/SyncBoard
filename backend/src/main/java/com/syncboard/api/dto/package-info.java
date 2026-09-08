// Stage 2 — REST API DTOs
// Package-wide Jackson rule: null fields are omitted from JSON responses, per
// contract 01-CONTRACT.md §4 ("omitted from JSON when null").
@JsonInclude(JsonInclude.Include.NON_NULL)
package com.syncboard.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
