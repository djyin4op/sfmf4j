/*
 * Copyright 2012 Steven Swor.
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
package sfmf4j.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sfmf4j.api.DirectoryListener;
import sfmf4j.api.FileMonitorService;

/**
 * Parent class for testing implementations.
 *
 * @author sswor
 */
public abstract class AbstractSFMF4JTest {

    /**
     * The timeout duration when verifying events have been fired.
     * @return the timeout duration when verifying events have been fired
     */
    protected long eventTimeoutDuration() {
        return 10;
    }

    /**
     * The timeout time unit when verifying events have been fired.
     * @return the timout time unit when verifying events have been fired
     */
    protected TimeUnit eventTimeoutTimeUnit() {
        return TimeUnit.SECONDS;
    }

    /**
     * Temporary folder used for creating, modifying, and deleting files and
     * folders.
     */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Performs the tests for the implementation by creating, modifying, and
     * deleting files.
     * @param fileMonitor the implementation to test
     * @throws Throwable if the test fails
     */
    protected void doTestFileMonitoring(final FileMonitorService fileMonitor) throws Throwable {
        final Logger logger = LoggerFactory.getLogger(getClass());
        File newFile = null;
        final BlockingQueue<File> createdFiles = new LinkedBlockingQueue<File>();
        final BlockingQueue<File> modifiedFiles = new LinkedBlockingQueue<File>();
        final BlockingQueue<File> deletedFiles = new LinkedBlockingQueue<File>();
        final DirectoryListener listener = new DirectoryListener() {
                public void fileCreated(File created) {
                    createdFiles.add(created);
                }

                public void fileChanged(File changed) {
                    modifiedFiles.add(changed);
                }

                public void fileDeleted(File deleted) {
                    deletedFiles.add(deleted);
                }
            };
        File folder = null;
        try {
            folder = tempFolder.getRoot();
            fileMonitor.registerDirectoryListener(folder, listener);

            /*
             * Create a new file.
             */
            newFile = tempFolder.newFile();
            logger.debug("Test file: {}", newFile.getAbsolutePath());
            logger.info("Testing for created event.");
            File created = createdFiles.poll(eventTimeoutDuration(), eventTimeoutTimeUnit());
            assertNotNull(created);
            assertEquals(newFile.getAbsolutePath(), created.getAbsolutePath());

            /*
             * Write to the file.
             */
            logger.info("Testing for modification event.");
            FileOutputStream fileOut = null;
            byte[] bytes = new byte[4096];
            try {
                fileOut = new FileOutputStream(newFile);
                fileOut.write(bytes);
            } finally {
                if (fileOut != null) {
                    try {
                        fileOut.close();
                    } catch (Exception ex) {
                        //trap.  We'll have bigger fish to fry if this happens
                    }
                }
            }
            File modified = modifiedFiles.poll(eventTimeoutDuration(), eventTimeoutTimeUnit());
            assertNotNull(modified);
            assertEquals(newFile.getAbsolutePath(), modified.getAbsolutePath());
            /*
             * Test deletion.
             */
            newFile.delete();
            File deleted = deletedFiles.poll(eventTimeoutDuration(), eventTimeoutTimeUnit());
            assertNotNull(deleted);
            assertEquals(newFile.getAbsolutePath(), deleted.getAbsolutePath());
        } finally {
            if (folder!=null) {
                try {
                    fileMonitor.unregisterDirectoryListener(folder, listener);
                }catch(Exception ex) {
                    //trap
                }
            }
            if (newFile != null && newFile.exists()) {
                try {
                    newFile.delete();
                }catch(Exception ex) {
                    //trap
                }
            }
        }

    }
}
