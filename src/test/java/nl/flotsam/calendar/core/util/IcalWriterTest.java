package nl.flotsam.calendar.core.util;

import com.google.appengine.api.urlfetch.HTTPResponse;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IcalWriterTest {

    @Mock
    private HTTPResponse response;

    @Test
    public void shouldSupportJapanese() throws IOException {
        Calendar calendar = new Calendar();
        Resource resource = new ClassPathResource("/japanese.ics");
        when(response.getResponseCode()).thenReturn(200);
        when(response.getContent()).thenReturn(IOUtils.toByteArray(resource.getInputStream()));
        calendar = IcalWriter.merge(calendar, response);
        assertThat(calendar.getComponents().size(), greaterThan(0));
        assertThat(calendar.getComponents().get(0), instanceOf(VEvent.class));
        VEvent event = (VEvent) calendar.getComponents().get(0);
        String snippet = "\u7b2c\uff18\uff12\u56de";
        assertThat(event.getDescription().getValue(), containsString(snippet));
    }

}
