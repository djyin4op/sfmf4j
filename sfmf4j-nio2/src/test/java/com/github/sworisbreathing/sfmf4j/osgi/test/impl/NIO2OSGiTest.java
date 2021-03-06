/*
 * Copyright 2012-2013 Steven Swor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sworisbreathing.sfmf4j.osgi.test.impl;

import com.github.sworisbreathing.sfmf4j.osgi.test.AbstractOSGiTest;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

/**
 *
 * @author sswor
 */
public class NIO2OSGiTest extends AbstractOSGiTest {
    
    protected boolean isPollingImplementation() {
        boolean results = false;
        String osName = System.getProperty("os.name");
        if (osName.contains("Mac OS X")) {
            results = true;
        }
        return results;
    }

    @Override
    protected long eventTimeoutDuration() {
        long results = super.eventTimeoutDuration();
        if (isPollingImplementation()) {
            results = 15L;
        }
        return results;
    }

    @Override
    protected Option implementationOption() {
        //return mavenBundle(sfmf4jGroupId(), "sfmf4j-nio2", sfmf4jVersion());
        return bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes");
    }

}
