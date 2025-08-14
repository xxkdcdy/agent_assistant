package com.ddlwlrma.ddlwlrmaaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DdlwlrmaManusTest {

    @Resource
    private DdlwlrmaManus ddlwlrmaManus;

    @Test
    void run() {
        String userPrompt = """  
                我的另一半居住在上海静安区，请帮我找到 5 公里内 3 个合适的约会地点，  
                每个地点结合 1 张网络图片，制定一份详细的约会计划，  
                并以 PDF 格式输出""";
        String answer = ddlwlrmaManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }

}
