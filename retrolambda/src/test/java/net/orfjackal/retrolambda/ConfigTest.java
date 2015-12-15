// Copyright © 2013-2015 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.retrolambda;

import org.junit.*;
import org.junit.rules.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ConfigTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private final Properties systemProperties = new Properties();

    private Config config() {
        return new Config(systemProperties);
    }

    @Test
    public void bytecode_version() {
        assertThat("defaults to Java 7", config().getBytecodeVersion(), is(51));
        assertThat("human printable format", config().getJavaVersion(), is("Java 7"));

        systemProperties.setProperty(Config.BYTECODE_VERSION, "50");
        assertThat("can override the default", config().getBytecodeVersion(), is(50));
        assertThat("human printable format", config().getJavaVersion(), is("Java 6"));
    }

    @Test
    public void default_methods() {
        assertThat("defaults to disabled", config().isDefaultMethodsEnabled(), is(false));

        systemProperties.setProperty(Config.DEFAULT_METHODS, "true");
        assertThat("can override the default", config().isDefaultMethodsEnabled(), is(true));
    }

    @Test
    public void input_directory_is_required() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Missing required property: retrolambda.inputDir");
        config().getInputDir();
    }

    @Test
    public void output_directory() {
        systemProperties.setProperty(Config.INPUT_DIR, "input dir");
        assertThat("defaults to input dir", config().getOutputDir(), is(Paths.get("input dir")));

        systemProperties.setProperty(Config.OUTPUT_DIR, "output dir");
        assertThat("can override the default", config().getOutputDir(), is(Paths.get("output dir")));
    }

    @Test
    public void classpath() {
        systemProperties.setProperty(Config.CLASSPATH, "");
        assertThat("zero values", config().getClasspath(), is(empty()));

        systemProperties.setProperty(Config.CLASSPATH, "one.jar");
        assertThat("one value", config().getClasspath(), is(Arrays.asList(Paths.get("one.jar"))));

        systemProperties.setProperty(Config.CLASSPATH, "one.jar" + File.pathSeparator + "two.jar");
        assertThat("multiple values", config().getClasspath(), is(Arrays.asList(Paths.get("one.jar"), Paths.get("two.jar"))));
    }

    @Ignore // TODO
    @Test
    public void classpath_file() {
    }

    @Test
    public void classpath_is_required() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Missing required property: retrolambda.classpath");
        config().getClasspath();
    }

    @Test
    public void included_files() {
        assertThat("not set", config().getIncludedFiles(), is(nullValue()));

        systemProperties.setProperty(Config.INCLUDED_FILES, "");
        assertThat("zero values", config().getIncludedFiles(), is(empty()));

        systemProperties.setProperty(Config.INCLUDED_FILES, "/foo/one.class");
        assertThat("one value", config().getIncludedFiles(), is(Arrays.asList(Paths.get("/foo/one.class"))));

        systemProperties.setProperty(Config.INCLUDED_FILES, "/foo/one.class" + File.pathSeparator + "/foo/two.class");
        assertThat("multiple values", config().getIncludedFiles(), is(Arrays.asList(Paths.get("/foo/one.class"), Paths.get("/foo/two.class"))));
    }

    @Test
    public void included_files_file() throws IOException {
        Path listFile = tempDir.newFile("list.txt").toPath();
        assertThat("not set", config().getIncludedFiles(), is(nullValue()));

        Files.write(listFile, Arrays.asList("", "", "")); // empty lines are ignored
        systemProperties.setProperty(Config.INCLUDED_FILES_FILE, listFile.toString());
        assertThat("zero values", config().getIncludedFiles(), is(empty()));

        Files.write(listFile, Arrays.asList("one.class"));
        systemProperties.setProperty(Config.INCLUDED_FILES_FILE, listFile.toString());
        assertThat("one value", config().getIncludedFiles(), is(Arrays.asList(Paths.get("one.class"))));

        Files.write(listFile, Arrays.asList("one.class", "two.class"));
        systemProperties.setProperty(Config.INCLUDED_FILES_FILE, listFile.toString());
        assertThat("multiple values", config().getIncludedFiles(), is(Arrays.asList(Paths.get("one.class"), Paths.get("two.class"))));
    }
}