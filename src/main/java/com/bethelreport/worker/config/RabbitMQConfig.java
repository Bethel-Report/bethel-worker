package com.bethelreport.worker.config;

import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	@Bean
	MessageConverter jacksonJsonMessageConverter() {
		JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
		DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
		typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
		typeMapper.setTrustedPackages("*");
		converter.setJavaTypeMapper(typeMapper);
		return converter;
	}
}
