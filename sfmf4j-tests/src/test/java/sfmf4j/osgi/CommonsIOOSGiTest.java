/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sfmf4j.osgi;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import org.ops4j.pax.exam.Option;

/**
 *
 * @author sswor
 */
public class CommonsIOOSGiTest extends AbstractOSGiTest {

    public CommonsIOOSGiTest(){
        super(new SFMF4JImplementationOptionBuilder() {

            @Override
            public Option sfmf4jOption() {
                return mavenBundle("sfmf4j", "sfmf4j-commonsio", "1.0-SNAPSHOT");
            }

            @Override
            public String getExpectedClassName() {
                return "sfmf4j.commonsio.CommonsIOFileMonitorServiceFactory";
            }
            
            
        });
    }
}
