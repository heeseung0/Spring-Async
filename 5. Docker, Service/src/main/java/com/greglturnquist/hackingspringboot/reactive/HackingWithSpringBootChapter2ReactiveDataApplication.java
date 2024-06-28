package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.Bean;

import org.bson.Document;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

@SpringBootApplication
public class HackingWithSpringBootChapter2ReactiveDataApplication {
	public static void main(String[] args) {
		SpringApplication.run(HackingWithSpringBootChapter2ReactiveDataApplication.class, args);
	}

	//httpTraceWebFilter Bean은 하나만 설정
	HttpTraceRepository traceRepository() { // <2>
		return new InMemoryHttpTraceRepository(); // <3>
	}

	@Bean
	HttpTraceRepository springDataTraceRepository(HttpTraceWrapperRepository repository){
		return new SpringDataHttpTraceRepository(repository);
	}

	/*
		람다로 교체하면 안된다.
		SpringData는 제네릭 파라미터 기준으로 적절한 컨버터를 판별하고 찾아서 사용하는데,
		람다로 교체하면 자바의 타입 소거(type erasure) 규칙에 의해
		제너릭 파라미터가 소거되므로, 컨버터로 사용할 수 없게 된다.
	 */
	static Converter<Document, HttpTraceWrapper> CONVERTER = new Converter<Document, HttpTraceWrapper>() {
		@Override
		public HttpTraceWrapper convert(Document document) {
			Document httpTrace = document.get("httpTrace", Document.class);
			Document request = httpTrace.get("request", Document.class);
			Document response = httpTrace.get("response", Document.class);

			return new HttpTraceWrapper(
				new HttpTrace(    //HttpTrace(request, response, timestamp, principal, session, timeTaken)
					new HttpTrace.Request(    //Request(method, uri, headers, remoteAddress)
						request.getString("method"),
						URI.create(request.getString("uri")),
						request.get("headers", Map.class),
						null
					),
					new HttpTrace.Response(    //Response(status, headers)
						response.getInteger("status"),
						response.get("headers", Map.class)
					),
					httpTrace.getDate("timestamp").toInstant(),
					null,
					null,
					httpTrace.getLong("timeTaken")
				)
			);
		}
	};

	@Bean
	public MappingMongoConverter mappingMongoConverter(MongoMappingContext context) {
		//몽고디비의 DBRef 값에 해석이 필요할 때 UnsupportedOperationException을 던지는
		//NoOpDbRefResolver를 사용해서 MappingMongoConverter 객체를 생성한다.
		//HttpTrace에는 DBRef 객체가 없으므로 DBRef 값 해석이 발생하지 않으며, NoOpRefResolver에 의해 예외가 발생하지 않는다.
		//단지 MappingMongoConverter의 생성자가, DbRefResolver 타입의 인자를 받기 때문에 NoOpDbRefResolver를 전달하는 것이다.
		MappingMongoConverter mappingConverter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
		
		//MongoCustomConversions 객체를 mappingConverter에 설정.
		//커스텀 컨버터 한 개로 구성된 리스트를 추가해서 MongoCustomConversions를 생성
		mappingConverter.setCustomConversions(new MongoCustomConversions(Collections.singletonList(CONVERTER)));

		return mappingConverter;
	}
}
