/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oozie.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class TestCLIParser extends TestCase {

    public void testEmptyParser() throws Exception {
        try {
            CLIParser parser = new CLIParser("oozie", new String[]{});
            CLIParser.Command c = parser.parse(new String[]{"a"});
            fail();
        }
        catch (ParseException ex) {
            //nop
        }
    }

    public void testCommandParser() throws Exception {
        try {
            CLIParser parser = new CLIParser("oozie", new String[]{});
            parser.addCommand("a", "<A>", "AAAAA", new Options(), false);
            CLIParser.Command c = parser.parse(new String[]{"a", "b"});
            assertEquals("a", c.getName());
            assertEquals("b", c.getCommandLine().getArgs()[0]);
        }
        catch (ParseException ex) {
            fail();
        }
    }

    public void testCommandParserX() throws Exception {
        Option opt = new Option("o", false, "O");
        Options opts = new Options();
        opts.addOption(opt);
        CLIParser parser = new CLIParser("test", new String[]{});
        parser.addCommand("c", "-X ", "(everything after '-X' are pass-through parameters)", opts, true);
        CLIParser.Command c = parser.parse("c -o -X -o c".split(" "));
        assertEquals("-X", c.getCommandLine().getArgList().get(0));
        assertEquals(3, c.getCommandLine().getArgList().size());
    }

    public void testCommandParserShowHelp() throws Exception {
        String pattern = "ozzie job <A>: AAAAA";
        CLIParser parser = new CLIParser("ozzie", new String[]{});
        parser.addCommand("job", "<A>", "AAAAA", new Options(), false);
        CLIParser.Command c = parser.parse(new String[]{"job", "b"});
        ByteArrayOutputStream outBytes = readCommandOutput(parser, c);
        assertTrue(new String(outBytes.toByteArray()).contains(pattern));
    }

    public void testCommandParserShowHelpWithOptions() throws Exception {
        String pattern = "ozzie job <OPTIONS> : job operations";
        CLIParser parser = new CLIParser("ozzie", new String[]{});
        parser.addCommand("job", "", "job operations", createCommandOptions(), false);
        CLIParser.Command c = parser.parse(new String[]{ "job", "-url", "test-name", "-verbose" });
        ByteArrayOutputStream outBytes = readCommandOutput(parser, c);
        assertTrue(new String(outBytes.toByteArray()).contains(pattern));
    }

    private Options createCommandOptions() {
        Option oozie = new Option("url", true, "URL");
        Option name = new Option("verbose", false, "Name");
        Options complexOptions = new Options();
        complexOptions.addOption(oozie);
        complexOptions.addOption(name);
        return complexOptions;
    }

    private ByteArrayOutputStream readCommandOutput(CLIParser parser,
            CLIParser.Command c) throws IOException {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut, 1024 * 10);
        System.setOut(new PrintStream(pipeOut));

        parser.showHelp(c.getCommandLine());
        pipeOut.close();
        copyByteStream(pipeIn, outBytes);
        pipeIn.close();
        return outBytes;
    }

    private static void copyByteStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 4];
        int read;
        while ((read = in.read(buffer)) > -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.close();
    }
}
