package nl.flotsam.calendar.web;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CalendarAsStringHttpMessageConverter extends AbstractHttpMessageConverter<String> {

    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz == String.class;
    }

    @Override
    protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return IOUtils.toString(inputMessage.getBody(), "UTF-8");
    }

    @Override
    protected void writeInternal(String s, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        IOUtils.write(s, outputMessage.getBody(), "UTF-8");
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(new MediaType("text", "calendar"));
    }

}
