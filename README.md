Table of Contents

- [Description](#description)
- [Note](#note)
    - [Compatibility](#compatibility)
    - [Sample](#sample)
    - [Dependency](#dependency)
- [Event publish](#event-publish)
- [Event consume](#event-consume)
- [Future releases](#future-releases)

---

## Description

janitor는 기존 daysboy의 다음 버전입니다. 전체 기능 중 이벤트 발행 및 수신 후 이벤트 핸들러롤 호출해 주는 로직만 별도로 분리한 모듈입니다.  
janitor는 `EventStream` 클래스의 `publishEvent` 메소드를 사용하여 이벤트를 발행합니다. ~~이벤트 발행은 Transactional Outbox 패턴을 사용합니다.~~

## Note

janitor는 accent 3.0.0-SNAPSHOT을 사용하고 해당 accent는 json 직렬화를 위해 gson이 아닌 fasterxml을 사용합니다. fasterxml은 gson과 달리 json 역직렬화 시 빈 생성자를 사용하므로 Domain Event와 Data Event에 빈 생성자를 반드시 추가해야 합니다.

### Compatibility

1. `@JanitorEventHander`와 `EventStreamService`는 유지합니다. (토의 필요)
2. `application.yml`은 별도로 수정하지 않습니다.

### Sample

publisher/consumer 프로젝트를 참고합니다.

### Dependency

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>io.naraway</groupId>
            <artifactId>janitor-jpa-maria</artifactId>
            <version>3.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>io.naraway</groupId>
            <artifactId>janitor-relay-kafka</artifactId>
            <version>3.0.0-SNAPSHOT</version>
        </dependency>
        ...
    </dependencies>
</project>
```
## Event publish

이벤트의 발행은 빈으로 등록되는 `EventStream`을 이용합니다.

```java
@Service
@Transactional
public class TenantFlowLogic {
    //
    private final TenantAggregateLogic tenantAggregateLogic;
    private final EventStream eventStream;
    
    public TenantFlowLogic(TenantAggregateLogic tenantAggregateLogic,
                           EventStream eventStream) {
        //
        this.tenantAggregateLogic = tenantAggregateLogic;
        this.eventStream = eventStream;
    }
    
    public String registerTenant(RegisterTenantCommand command) {
        //
        this.tenantAggregateLogic.registerTenant(...);
        
        // publish event
        TenantRegisteredEvent event = new TenantRegisteredEvent(...);
        this.eventStream.publishEvent(event);
    }
}
```

## Event consume

janitor에서 사용하는 다양한 annotation은 제거되었으며 이벤트 핸들러인 `@EventHandler`만 유지합니다. 기존 라이브러리와의 호환성을 유지하기 위하여 package는 동일합니다.

```java
@Component
public class DomainEventHandler {
    //
    @EventHandler
    public void on(TenantRegisteredEvent event) {
        //
        System.out.println("DomainEventHandler.on(TenantRegisteredEvent): " + JsonUtil.toJson(event));
    }
}
```

## Future releases

향후 추가될 라이브러리는 다음과 같습니다.

| Artifact                 | Comment                                                           |
| ---                      | ---                                                               |
| janitor-relay-standalone | 카프카와 연결되지 않은 상태에서 이벤트 발행 및 소비를 테스트한다. |
|                          | maven 또는 gradle에서 profile로 의존성을 관리한다.                |
| janitor-jpa-*            | 다양한 데이터베이스 스토어를 지원한다.                            |
| janitor-relay-*          | 다양한 Broker를 지원한다.                                         |
