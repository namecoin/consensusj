package com.msgilligan.jsonrpc.ratpack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Provides;
import com.msgilligan.bitcoinj.json.conversion.RpcClientModule;
import com.msgilligan.bitcoinj.json.conversion.RpcServerModule;
import com.msgilligan.jsonrpc.ratpack.authext.BasicAuthCallFactory;
import com.msgilligan.bitcoinj.rpc.RPCConfig;
import ratpack.guice.ConfigurableModule;
import ratpack.retrofit.RatpackRetrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Create an asynchronous JSON RPC Client using RatpackRetrofit
 * TODO: Remove specific dependencies on Bitcoin
 */
public class JsonRpcClientModule extends ConfigurableModule<RPCConfig> {
    @Override
    protected void configure() {
    }

    @Provides
    ObjectMapper provideObjectMapper(RPCConfig config) {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new RpcClientModule(config.getNetParams()))
                .registerModule(new RpcServerModule());
    }
    
    @Provides
    JacksonConverterFactory provideJacksonConverterFactory(ObjectMapper mapper) {
        return JacksonConverterFactory.create(mapper);
    }

    @Provides
    JsonRpcClient provideJsonRpcClient(JacksonConverterFactory converterFactory,
                                       okhttp3.Call.Factory callFactory,
                                       RPCConfig rpcConfig) {
        return RatpackRetrofit
                .client(rpcConfig.getURI())
                .configure(b -> {
                    b.addConverterFactory(converterFactory);
                    b.callFactory(callFactory);
                })
                .build(JsonRpcClient.class);
    }

    @Provides
    okhttp3.Call.Factory provideCallFactory(RPCConfig config) {
        return new BasicAuthCallFactory(config.getUsername(), config.getPassword());
    }

}
