import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;

public class CustomRetryPolicy extends SimpleRetryPolicy {

    public CustomRetryPolicy() {
        super();
        setMaxAttempts(3); // Set the maximum number of retry attempts
    }

    @Override
    public boolean canRetry(RetryContext context) {
        // Customize the conditions for retrying, e.g., based on exception type
        return super.canRetry(context) && (context.getLastThrowable() instanceof TokenExpiredException);
    }
}


public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException(String message) {
        super(message);
    }
}



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Set a custom response error handler
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());

        // Set a retry template with exponential back-off policy
        restTemplate.setRetryTemplate(createRetryTemplate());

        return restTemplate;
    }

    private RetryTemplate createRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Set the retry policy
        CustomRetryPolicy retryPolicy = new CustomRetryPolicy();
        retryTemplate.setRetryPolicy(retryPolicy);

        // Set the back-off policy (exponential back-off)
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMaxInterval(30000);
        backOffPolicy.setMultiplier(2);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}

