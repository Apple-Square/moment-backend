package applesquare.moment.notification.strategy.registry;

import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.strategy.NotificationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationStrategyRegistry {
    private final Map<NotificationType, NotificationStrategy> strategyMap;

    @Autowired
    public NotificationStrategyRegistry(List<NotificationStrategy> strategies){
        this.strategyMap=strategies.stream().collect(Collectors.toMap(NotificationStrategy::getSupportedType, Function.identity()));
    }

    /**
     * 알림 유형에 따라 알림 전략 조회
     * @param notificationType 알림 유형
     * @return 알림 전략
     */
    public NotificationStrategy getStrategy(NotificationType notificationType){
        NotificationStrategy strategy=strategyMap.get(notificationType);
        if(strategy==null){
            throw new RuntimeException("전략이 등록되지 않은 알림 유형입니다. (type = "+notificationType+")");
        }
        return strategy;
    }
}
