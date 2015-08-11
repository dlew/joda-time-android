/*
 * Copyright 2015 Trello, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.danlew.android.joda

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.InputFileDetails
import org.joda.time.tz.ZoneInfoCompiler
import sun.util.calendar.ZoneInfo

/**
 * Converts raw TzData files into joda-time formatted files
 */
class CompileTzDataTask extends DefaultTask {

    @InputFiles
    File[] sources

    @OutputDirectory
    File outputDir

    @TaskAction
    def compile() {
        // Start fresh each time
        outputDir.delete()

        // Compile the data
        ZoneInfoCompiler zoneInfoCompiler = new ZoneInfoCompiler();
        zoneInfoCompiler.compile(outputDir, sources)
    }
}