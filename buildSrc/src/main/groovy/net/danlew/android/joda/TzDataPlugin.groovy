/*
 * Copyright 2015 Dan Lew
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Sync

class TzDataPlugin implements Plugin<Project> {

    final List<String> REGIONS = [
        'Africa',
        'America',
        'America/Argentina',
        'America/Kentucky',
        'America/Indiana',
        'America/North_Dakota',
        'Antarctica',
        'Arctic',
        'Asia',
        'Atlantic',
        'Australia',
        'Etc',
        'Europe',
        'Indian',
        'Pacific'
    ]

    void apply(Project project) {
        project.extensions.create('tzdata', TzDataPluginExtension)

        project.afterEvaluate {
            File tzDataDir = project.tzdata.tzDataDir

            File[] tzFiles = tzDataDir.listFiles(new FileFilter() {
                @Override
                boolean accept(File pathname) {
                    // Dumb check: if it has a dot, ignore it
                    return !pathname.absolutePath.contains(".")
                }
            })

            Task compileTask = project.task('compileTzData', type: CompileTzDataTask) {
                group = 'Timezone Data'
                description = 'Generates TZ data files from the source'

                sources = tzFiles
                outputDir = project.file("$project.buildDir/intermediates/tz/")
            }

            String resDir = "$project.buildDir/generated/tzdata/"

            Task reformatTask = project.task('reformatTzData', dependsOn: compileTask, type: Sync) {
                group = 'Timezone Data'
                description = 'Reformats the tzdata files for Android consumption'

                // Copy each region
                REGIONS.each { region ->
                    from("build/intermediates/tz/$region") {
                        exclude '*/*'
                        includeEmptyDirs false

                        rename { city ->
                            renameFile(region, city)
                        }
                    }
                }

                // Copy all root directory files (regionless)
                from("build/intermediates/tz/") {
                    rename { city ->
                        renameFile(null, city)
                    }

                    exclude '*/*'
                    includeEmptyDirs false
                }

                into "$resDir/raw"
            }

            project.android.libraryVariants.all { variant ->
                variant.registerResGeneratingTask(reformatTask, project.file(resDir))
            }
        }
    }

    String renameFile(String region, String city) {
        city = city.toLowerCase().replace('+', 'plus').replace('-', '_')
        if (region) {
            region = region.toLowerCase().replace('/', '_')
            'joda_' + region + '_' + city
        }
        else {
            'joda_' + city
        }
    }
}