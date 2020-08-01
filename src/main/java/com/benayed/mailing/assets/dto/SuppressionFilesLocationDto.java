package com.benayed.mailing.assets.dto;

import java.nio.file.Path;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SuppressionFilesLocationDto {
	private Path domainsPath;
	private Path dataPath;
}
