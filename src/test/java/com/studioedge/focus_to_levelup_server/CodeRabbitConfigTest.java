package com.studioedge.focus_to_levelup_server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive validation tests for .coderabbit.yaml configuration file.
 * 
 * These tests ensure that:
 * - The YAML file is valid and parseable
 * - Required configuration keys are present
 * - Critical values are set correctly
 * - Path instructions follow the expected format
 * - Tool configurations are properly structured
 */
@DisplayName("CodeRabbit Configuration Validation Tests")
class CodeRabbitConfigTest {

    private Map<String, Object> config;
    private static final String CONFIG_FILE_PATH = ".coderabbit.yaml";

    @BeforeEach
    void setUp() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream(CONFIG_FILE_PATH);
        config = yaml.load(inputStream);
        assertNotNull(config, "Configuration file should be loaded successfully");
    }

    @Nested
    @DisplayName("Basic Configuration Structure Tests")
    class BasicStructureTests {

        @Test
        @DisplayName("Should have version field set to '2'")
        void shouldHaveValidVersion() {
            assertTrue(config.containsKey("version"), "Configuration should contain 'version' key");
            assertEquals("2", config.get("version"), "Version should be set to '2'");
        }

        @Test
        @DisplayName("Should have language configured")
        void shouldHaveLanguageConfigured() {
            assertTrue(config.containsKey("language"), "Configuration should contain 'language' key");
            String language = (String) config.get("language");
            assertNotNull(language, "Language should not be null");
            assertTrue(language.equals("ko-KR") || language.equals("en-US"), 
                "Language should be either 'ko-KR' or 'en-US'");
        }

        @Test
        @DisplayName("Should have early_access enabled")
        void shouldHaveEarlyAccessEnabled() {
            assertTrue(config.containsKey("early_access"), "Configuration should contain 'early_access' key");
            assertTrue((Boolean) config.get("early_access"), "early_access should be enabled");
        }

        @Test
        @DisplayName("Should have enable_free_tier configured")
        void shouldHaveFreeTierConfigured() {
            assertTrue(config.containsKey("enable_free_tier"), "Configuration should contain 'enable_free_tier' key");
            assertTrue((Boolean) config.get("enable_free_tier"), "enable_free_tier should be enabled");
        }
    }

    @Nested
    @DisplayName("Reviews Configuration Tests")
    class ReviewsConfigTests {

        private Map<String, Object> reviewsConfig;

        @BeforeEach
        void setUpReviews() {
            assertTrue(config.containsKey("reviews"), "Configuration should contain 'reviews' section");
            reviewsConfig = (Map<String, Object>) config.get("reviews");
            assertNotNull(reviewsConfig, "Reviews configuration should not be null");
        }

        @Test
        @DisplayName("Should have valid profile setting")
        void shouldHaveValidProfile() {
            assertTrue(reviewsConfig.containsKey("profile"), "Reviews should contain 'profile' key");
            String profile = (String) reviewsConfig.get("profile");
            assertTrue(List.of("chill", "assertive").contains(profile), 
                "Profile should be either 'chill' or 'assertive'");
        }

        @Test
        @DisplayName("Should have request_changes_workflow configured")
        void shouldHaveRequestChangesWorkflow() {
            assertTrue(reviewsConfig.containsKey("request_changes_workflow"), 
                "Reviews should contain 'request_changes_workflow' key");
            assertFalse((Boolean) reviewsConfig.get("request_changes_workflow"), 
                "request_changes_workflow should be false for solo developer");
        }

        @Test
        @DisplayName("Should have summary features enabled")
        void shouldHaveSummaryFeaturesEnabled() {
            assertTrue(reviewsConfig.containsKey("high_level_summary"), 
                "Reviews should contain 'high_level_summary' key");
            assertTrue((Boolean) reviewsConfig.get("high_level_summary"), 
                "high_level_summary should be enabled");
            
            assertTrue(reviewsConfig.containsKey("high_level_summary_in_walkthrough"), 
                "Reviews should contain 'high_level_summary_in_walkthrough' key");
            assertTrue((Boolean) reviewsConfig.get("high_level_summary_in_walkthrough"), 
                "high_level_summary_in_walkthrough should be enabled");
        }

        @Test
        @DisplayName("Should have sequence diagrams enabled")
        void shouldHaveSequenceDiagramsEnabled() {
            assertTrue(reviewsConfig.containsKey("sequence_diagrams"), 
                "Reviews should contain 'sequence_diagrams' key");
            assertTrue((Boolean) reviewsConfig.get("sequence_diagrams"), 
                "sequence_diagrams should be enabled");
        }

        @Test
        @DisplayName("Should have poem disabled")
        void shouldHavePoemDisabled() {
            assertTrue(reviewsConfig.containsKey("poem"), "Reviews should contain 'poem' key");
            assertFalse((Boolean) reviewsConfig.get("poem"), "poem should be disabled");
        }

        @Test
        @DisplayName("Should have labeling automation enabled")
        void shouldHaveLabelingEnabled() {
            assertTrue(reviewsConfig.containsKey("suggested_labels"), 
                "Reviews should contain 'suggested_labels' key");
            assertTrue((Boolean) reviewsConfig.get("suggested_labels"), 
                "suggested_labels should be enabled");
            
            assertTrue(reviewsConfig.containsKey("auto_apply_labels"), 
                "Reviews should contain 'auto_apply_labels' key");
            assertTrue((Boolean) reviewsConfig.get("auto_apply_labels"), 
                "auto_apply_labels should be enabled");
        }

        @Test
        @DisplayName("Should have path filters configured")
        void shouldHavePathFilters() {
            assertTrue(reviewsConfig.containsKey("path_filters"), 
                "Reviews should contain 'path_filters' key");
            List<String> pathFilters = (List<String>) reviewsConfig.get("path_filters");
            assertNotNull(pathFilters, "Path filters should not be null");
            assertFalse(pathFilters.isEmpty(), "Path filters should not be empty");
            
            // Verify critical exclusions
            assertTrue(pathFilters.stream().anyMatch(f -> f.contains("build")), 
                "Should exclude build directories");
            assertTrue(pathFilters.stream().anyMatch(f -> f.contains("gradle")), 
                "Should exclude gradle directories");
        }

        @Test
        @DisplayName("Should have path instructions configured")
        void shouldHavePathInstructions() {
            assertTrue(reviewsConfig.containsKey("path_instructions"), 
                "Reviews should contain 'path_instructions' key");
            List<Map<String, String>> pathInstructions = 
                (List<Map<String, String>>) reviewsConfig.get("path_instructions");
            assertNotNull(pathInstructions, "Path instructions should not be null");
            assertFalse(pathInstructions.isEmpty(), "Path instructions should not be empty");
        }

        @Test
        @DisplayName("Should have JPA Entity conventions in path instructions")
        void shouldHaveEntityConventions() {
            List<Map<String, String>> pathInstructions = 
                (List<Map<String, String>>) reviewsConfig.get("path_instructions");
            
            boolean hasEntityPath = pathInstructions.stream()
                .anyMatch(instruction -> {
                    String path = instruction.get("path");
                    return path != null && (path.contains("entity") || path.contains("domain"));
                });
            
            assertTrue(hasEntityPath, "Should have path instructions for entity classes");
        }

        @Test
        @DisplayName("Should have Controller conventions in path instructions")
        void shouldHaveControllerConventions() {
            List<Map<String, String>> pathInstructions = 
                (List<Map<String, String>>) reviewsConfig.get("path_instructions");
            
            boolean hasControllerPath = pathInstructions.stream()
                .anyMatch(instruction -> {
                    String path = instruction.get("path");
                    return path != null && path.contains("Controller.java");
                });
            
            assertTrue(hasControllerPath, "Should have path instructions for Controller classes");
        }

        @Test
        @DisplayName("Should have timezone and logging guidance for Java files")
        void shouldHaveJavaFileGuidance() {
            List<Map<String, String>> pathInstructions = 
                (List<Map<String, String>>) reviewsConfig.get("path_instructions");
            
            boolean hasJavaPath = pathInstructions.stream()
                .anyMatch(instruction -> {
                    String path = instruction.get("path");
                    return path != null && path.endsWith(".java");
                });
            
            assertTrue(hasJavaPath, "Should have path instructions for general Java files");
        }
    }

    @Nested
    @DisplayName("Chat Configuration Tests")
    class ChatConfigTests {

        @Test
        @DisplayName("Should have auto_reply enabled")
        void shouldHaveAutoReplyEnabled() {
            assertTrue(config.containsKey("chat"), "Configuration should contain 'chat' section");
            Map<String, Object> chatConfig = (Map<String, Object>) config.get("chat");
            assertNotNull(chatConfig, "Chat configuration should not be null");
            assertTrue(chatConfig.containsKey("auto_reply"), "Chat should contain 'auto_reply' key");
            assertTrue((Boolean) chatConfig.get("auto_reply"), "auto_reply should be enabled");
        }
    }

    @Nested
    @DisplayName("Code Generation Configuration Tests")
    class CodeGenerationConfigTests {

        private Map<String, Object> codeGenConfig;

        @BeforeEach
        void setUpCodeGen() {
            assertTrue(config.containsKey("code_generation"), 
                "Configuration should contain 'code_generation' section");
            codeGenConfig = (Map<String, Object>) config.get("code_generation");
            assertNotNull(codeGenConfig, "Code generation configuration should not be null");
        }

        @Test
        @DisplayName("Should have docstrings configuration")
        void shouldHaveDocstringsConfig() {
            assertTrue(codeGenConfig.containsKey("docstrings"), 
                "Code generation should contain 'docstrings' section");
            Map<String, Object> docstringsConfig = (Map<String, Object>) codeGenConfig.get("docstrings");
            assertNotNull(docstringsConfig, "Docstrings configuration should not be null");
            assertTrue(docstringsConfig.containsKey("path_instructions"), 
                "Docstrings should contain 'path_instructions'");
        }

        @Test
        @DisplayName("Should have unit_tests configuration")
        void shouldHaveUnitTestsConfig() {
            assertTrue(codeGenConfig.containsKey("unit_tests"), 
                "Code generation should contain 'unit_tests' section");
            Map<String, Object> unitTestsConfig = (Map<String, Object>) codeGenConfig.get("unit_tests");
            assertNotNull(unitTestsConfig, "Unit tests configuration should not be null");
            assertTrue(unitTestsConfig.containsKey("path_instructions"), 
                "Unit tests should contain 'path_instructions'");
        }

        @Test
        @DisplayName("Should have Service class test instructions")
        void shouldHaveServiceTestInstructions() {
            Map<String, Object> unitTestsConfig = (Map<String, Object>) codeGenConfig.get("unit_tests");
            List<Map<String, String>> pathInstructions = 
                (List<Map<String, String>>) unitTestsConfig.get("path_instructions");
            
            boolean hasServicePath = pathInstructions.stream()
                .anyMatch(instruction -> {
                    String path = instruction.get("path");
                    String instructions = instruction.get("instructions");
                    return path != null && path.contains("Service.java") &&
                           instructions != null && instructions.contains("JUnit");
                });
            
            assertTrue(hasServicePath, 
                "Should have unit test instructions for Service classes mentioning JUnit");
        }
    }

    @Nested
    @DisplayName("Pre-merge Checks Configuration Tests")
    class PreMergeChecksTests {

        private Map<String, Object> preMergeConfig;

        @BeforeEach
        void setUpPreMerge() {
            assertTrue(config.containsKey("pre_merge_checks"), 
                "Configuration should contain 'pre_merge_checks' section");
            preMergeConfig = (Map<String, Object>) config.get("pre_merge_checks");
            assertNotNull(preMergeConfig, "Pre-merge checks configuration should not be null");
        }

        @Test
        @DisplayName("Should have all checks set to warning mode")
        void shouldHaveWarningMode() {
            assertTrue(preMergeConfig.containsKey("title"), "Should have title check");
            Map<String, String> titleCheck = (Map<String, String>) preMergeConfig.get("title");
            assertEquals("warning", titleCheck.get("mode"), "Title check should be in warning mode");

            assertTrue(preMergeConfig.containsKey("description"), "Should have description check");
            Map<String, String> descCheck = (Map<String, String>) preMergeConfig.get("description");
            assertEquals("warning", descCheck.get("mode"), "Description check should be in warning mode");
        }

        @Test
        @DisplayName("Should have docstrings threshold configured")
        void shouldHaveDocstringsThreshold() {
            assertTrue(preMergeConfig.containsKey("docstrings"), "Should have docstrings check");
            Map<String, Object> docstringsCheck = (Map<String, Object>) preMergeConfig.get("docstrings");
            assertEquals("warning", docstringsCheck.get("mode"), 
                "Docstrings check should be in warning mode");
            assertTrue(docstringsCheck.containsKey("threshold"), 
                "Docstrings check should have threshold");
            Integer threshold = (Integer) docstringsCheck.get("threshold");
            assertTrue(threshold >= 0 && threshold <= 100, 
                "Threshold should be a valid percentage");
        }
    }

    @Nested
    @DisplayName("Tools Configuration Tests")
    class ToolsConfigTests {

        private Map<String, Object> toolsConfig;

        @BeforeEach
        void setUpTools() {
            assertTrue(config.containsKey("tools"), "Configuration should contain 'tools' section");
            toolsConfig = (Map<String, Object>) config.get("tools");
            assertNotNull(toolsConfig, "Tools configuration should not be null");
        }

        @Test
        @DisplayName("Should have security tools enabled")
        void shouldHaveSecurityToolsEnabled() {
            assertTrue(toolsConfig.containsKey("gitleaks"), "Should have gitleaks configured");
            Map<String, Boolean> gitleaksConfig = (Map<String, Boolean>) toolsConfig.get("gitleaks");
            assertTrue(gitleaksConfig.get("enabled"), "Gitleaks should be enabled");

            assertTrue(toolsConfig.containsKey("osv-scanner"), "Should have osv-scanner configured");
            Map<String, Boolean> osvConfig = (Map<String, Boolean>) toolsConfig.get("osv-scanner");
            assertTrue(osvConfig.get("enabled"), "OSV-scanner should be enabled");
        }

        @Test
        @DisplayName("Should have Java static analysis tool enabled")
        void shouldHaveJavaToolsEnabled() {
            assertTrue(toolsConfig.containsKey("pmd"), "Should have PMD configured");
            Map<String, Boolean> pmdConfig = (Map<String, Boolean>) toolsConfig.get("pmd");
            assertTrue(pmdConfig.get("enabled"), "PMD should be enabled");
        }

        @Test
        @DisplayName("Should have infrastructure tools enabled")
        void shouldHaveInfraToolsEnabled() {
            assertTrue(toolsConfig.containsKey("hadolint"), "Should have hadolint configured");
            Map<String, Boolean> hadolintConfig = (Map<String, Boolean>) toolsConfig.get("hadolint");
            assertTrue(hadolintConfig.get("enabled"), "Hadolint should be enabled");

            assertTrue(toolsConfig.containsKey("checkov"), "Should have checkov configured");
            Map<String, Boolean> checkovConfig = (Map<String, Boolean>) toolsConfig.get("checkov");
            assertTrue(checkovConfig.get("enabled"), "Checkov should be enabled");
        }

        @Test
        @DisplayName("Should have linting tools enabled")
        void shouldHaveLintingToolsEnabled() {
            assertTrue(toolsConfig.containsKey("markdownlint"), "Should have markdownlint configured");
            Map<String, Boolean> mdlintConfig = (Map<String, Boolean>) toolsConfig.get("markdownlint");
            assertTrue(mdlintConfig.get("enabled"), "Markdownlint should be enabled");

            assertTrue(toolsConfig.containsKey("actionlint"), "Should have actionlint configured");
            Map<String, Boolean> actionlintConfig = (Map<String, Boolean>) toolsConfig.get("actionlint");
            assertTrue(actionlintConfig.get("enabled"), "Actionlint should be enabled");

            assertTrue(toolsConfig.containsKey("yamllint"), "Should have yamllint configured");
            Map<String, Boolean> yamllintConfig = (Map<String, Boolean>) toolsConfig.get("yamllint");
            assertTrue(yamllintConfig.get("enabled"), "Yamllint should be enabled");
        }

        @Test
        @DisplayName("Should have GitHub checks integration enabled")
        void shouldHaveGitHubChecksEnabled() {
            assertTrue(toolsConfig.containsKey("github-checks"), 
                "Should have github-checks configured");
            Map<String, Boolean> ghChecksConfig = (Map<String, Boolean>) toolsConfig.get("github-checks");
            assertTrue(ghChecksConfig.get("enabled"), "GitHub checks should be enabled");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Robustness Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should not contain null values in critical paths")
        void shouldNotContainNullValuesInCriticalPaths() {
            assertNotNull(config.get("version"), "Version should not be null");
            assertNotNull(config.get("language"), "Language should not be null");
            assertNotNull(config.get("reviews"), "Reviews section should not be null");
            assertNotNull(config.get("tools"), "Tools section should not be null");
        }

        @Test
        @DisplayName("Should have valid boolean values where expected")
        void shouldHaveValidBooleanValues() {
            Object earlyAccess = config.get("early_access");
            assertTrue(earlyAccess instanceof Boolean, "early_access should be a boolean");
            
            Object freeTier = config.get("enable_free_tier");
            assertTrue(freeTier instanceof Boolean, "enable_free_tier should be a boolean");
        }

        @Test
        @DisplayName("Should have non-empty path instructions")
        void shouldHaveNonEmptyPathInstructions() {
            Map<String, Object> reviewsConfig = (Map<String, Object>) config.get("reviews");
            List<Map<String, String>> pathInstructions = 
                (List<Map<String, String>>) reviewsConfig.get("path_instructions");
            
            for (Map<String, String> instruction : pathInstructions) {
                assertNotNull(instruction.get("path"), "Path should not be null");
                assertNotNull(instruction.get("instructions"), "Instructions should not be null");
                assertFalse(instruction.get("path").trim().isEmpty(), 
                    "Path should not be empty");
                assertFalse(instruction.get("instructions").trim().isEmpty(), 
                    "Instructions should not be empty");
            }
        }

        @Test
        @DisplayName("Should have at least minimum number of path instructions")
        void shouldHaveMinimumPathInstructions() {
            Map<String, Object> reviewsConfig = (Map<String, Object>) config.get("reviews");
            List<Map<String, String>> pathInstructions = 
                (List<Map<String, String>>) reviewsConfig.get("path_instructions");
            
            assertTrue(pathInstructions.size() >= 3, 
                "Should have at least 3 path instructions (Entity, Controller, Java)");
        }
    }
}