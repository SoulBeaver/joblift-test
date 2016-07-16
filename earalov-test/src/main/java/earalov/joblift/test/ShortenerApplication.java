package earalov.joblift.test;

import com.codahale.metrics.MetricRegistryListener;
import com.hmsonline.dropwizard.spring.SpringServiceConfiguration;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.ws.rs.Path;
import java.util.Map;

/**
 * Test application for shortening URLs.
 */
//TODO(earalov): cover with tests. Clean up links storage. Add couchbase health check
public class ShortenerApplication extends Application<SpringServiceConfiguration>{

    public static void main(final String[] args) throws Exception {
        new ShortenerApplication().run(args);
    }

    @Override
    public void run(SpringServiceConfiguration configuration, Environment environment) throws Exception {

        AnnotationConfigWebApplicationContext parent = new AnnotationConfigWebApplicationContext();
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");

        parent.refresh();
        parent.getBeanFactory().registerSingleton("configuration", configuration);
        parent.registerShutdownHook();
        parent.start();

        ctx.setParent(parent);
        ctx.refresh();
        ctx.registerShutdownHook();
        ctx.start();

        Map resources = ctx.getBeansWithAnnotation(Path.class);
        resources.entrySet().stream().forEach((entry)->{
            environment.jersey().register(((Map.Entry)entry).getValue());
        });
    }

}
