package config;

import model.ApiProvider;

public class ApiConfig {
    private static ApiProvider selectedProvider;
    private static String apiKey;

    public static void selectProvider(ApiProvider provider)
    {
        selectedProvider = provider;

        switch (provider) {
            case ALPHA_VANTAGE -> apiKey = "38K5G0K9RTXXNMVP";
            case TWELVE_DATA -> apiKey = "b6bd57774d974cfb8929058a7b70516f";
        }
    }
    public static ApiProvider getProvider() {
        return selectedProvider;
    }

    public static String getApiKey() {
        return apiKey;
    }

}
