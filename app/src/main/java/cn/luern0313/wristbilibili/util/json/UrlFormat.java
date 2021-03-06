package cn.luern0313.wristbilibili.util.json;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.luern0313.lson.annotation.LsonDefinedAnnotation;
import cn.luern0313.wristbilibili.util.DataProcessUtil;


/**
 * 被 luern0313 创建于 2020/7/31.
 */

@LsonDefinedAnnotation(config = UrlFormat.UrlHandleConfig.class, acceptableDeserializationType = LsonDefinedAnnotation.AcceptableType.STRING, acceptableSerializationType = LsonDefinedAnnotation.AcceptableType.STRING)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlFormat
{
    class UrlHandleConfig implements LsonDefinedAnnotation.LsonDefinedAnnotationConfig
    {
        @Override
        public Object deserialization(Object value, Annotation annotation, Object object)
        {
            return DataProcessUtil.handleUrl(value.toString());
        }

        @Override
        public Object serialization(Object value, Annotation annotation, Object object)
        {
            return value;
        }
    }
}
