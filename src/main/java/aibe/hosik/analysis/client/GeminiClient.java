package aibe.hosik.analysis.client;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {
    private final RestTemplate restTemplate;
    private final GeminiProperties geminiProperties;

    private String getApiKeyForModel(String model) {
        String key = geminiProperties.getModelKeys().get(model);
        if (key == null) {
            throw new IllegalArgumentException("해당 모델에 대한 API 키가 존재하지 않습니다: " + model);
        }
        return key;
    }

    // 모델 분리 4번 호출, 3개 모델 적용
    private static final String model1 = "gemini-2.0-flash-lite";
    private static final String model2 = "gemini-1.5-flash-8b";
    private static final String model3 = "gemini-2.0-flash";
    private static final String model4 = "gemini-2.0-flash";

    // 각 모델을 사용하여 Gemini API에 요청을 보냄
    public String generateContent(String prompt, String model) {
        try {
            String apiKey = getApiKeyForModel(model);
            String url = String.format("%s/models/%s:generateContent?key=%s", geminiProperties.getBaseUrl(), model, apiKey);

            // HTTP 헤더 설정 - JSON 컨텐츠 타입 지정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Gemini API 요청 본문 구성
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            // HTTP 요청 엔티티 생성 (헤더와 본문 포함)
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // API 호출 및 응답 받기
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            // 응답 처리
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> candidateContent = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) candidateContent.get("parts");
                    if (!parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }

            log.error("Failed to parse Gemini API response: {}", response);
            return "응답 파싱 실패";
        } catch (Exception e) {
            log.error("Error generating content from Gemini API", e);
            return "분석 과정에서 오류가 발생했습니다.";
        }
    }


    /**
     * 모델 1을 사용하여 스킬 및 성격 분석을 수행합니다.
     */
    public CompletableFuture<String> analysisWithModel1(String prompt) {
        return CompletableFuture.supplyAsync(() -> generateContent(prompt, model1));
    }

    /**
     * 모델 2를 사용하여 스킬 및 성격 분석을 수행합니다.
     */
    public CompletableFuture<String> analysisWithModel2(String prompt) {
        return CompletableFuture.supplyAsync(() -> generateContent(prompt, model2));
    }

    /**
     * 모델 3을 사용하여 자기소개서 요약을 수행합니다.
     */
    public CompletableFuture<String> summarizeResume(String prompt) {
        return CompletableFuture.supplyAsync(() -> generateContent(prompt, model3));
    }

    /**
     * 모델 4를 사용하여 모델 1 + 모델 2 결과를 종합 제공
     */
    public CompletableFuture<String> generateFinalAnalysis(String prompt) {
        return CompletableFuture.supplyAsync(() -> generateContent(prompt, model4));
    }
}
    

