package com.ddlwlrma.ddlwlrmaaiagent.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * https://arxiv.org/pdf/2309.06275
 * 重读一遍，可以提高效率
 */
public class MyReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {


	private AdvisedRequest before(AdvisedRequest advisedRequest) {

		String query = advisedRequest.userText();

		Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
		advisedUserParams.put("re2_input_query", query);

		advisedRequest.updateContext(context -> {
			context.put("user_query_re2", query + "请再次阅读一遍: " + query);
			return context;
		});

		return AdvisedRequest.from(advisedRequest)
			.userText(query + "请再次阅读一遍: " + query)
			.userParams(advisedUserParams)
			.build();
	}

	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		return chain.nextAroundCall(this.before(advisedRequest));
	}

	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		return chain.nextAroundStream(this.before(advisedRequest));
	}

	@Override
	public int getOrder() { 
		return 0;
	}

    @Override
    public String getName() { 
		return this.getClass().getSimpleName();
	}
}