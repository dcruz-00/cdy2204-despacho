package com.transportista.despacho.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "despacho.exchange";

    public static final String COLA_GUIAS = "cola.guias.despacho";
    public static final String COLA_GUIAS_ERROR = "cola.guias.error";

    public static final String ROUTING_KEY_GUIAS = "guias.routingkey";
    public static final String ROUTING_KEY_ERROR = "guias.error.routingkey";

    @Bean
    public DirectExchange despachoExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue colaGuias() {
        return new Queue(COLA_GUIAS, true); // durable
    }

    @Bean
    public Queue colaGuiasError() {
        return new Queue(COLA_GUIAS_ERROR, true); // durable
    }

    @Bean
    public Binding bindingColaGuias(Queue colaGuias, DirectExchange despachoExchange) {
        return BindingBuilder.bind(colaGuias).to(despachoExchange).with(ROUTING_KEY_GUIAS);
    }

    @Bean
    public Binding bindingColaGuiasError(Queue colaGuiasError, DirectExchange despachoExchange) {
        return BindingBuilder.bind(colaGuiasError).to(despachoExchange).with(ROUTING_KEY_ERROR);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}