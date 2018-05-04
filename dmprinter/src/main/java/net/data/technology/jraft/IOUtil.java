package net.data.technology.jraft;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;

public class IOUtil {
    public static int DEFAULT_BUFFER_SIZE = 4096;

    public static void copy(InputStream src, OutputStream dst, byte[] buffer) throws IOException {
        for (int len; (len = src.read(buffer)) != -1;)
            dst.write(buffer, 0, len);
    }

    public static void copy(InputStream src, OutputStream dst) throws IOException {
        copy(src, dst, new byte[DEFAULT_BUFFER_SIZE]);
    }

    public static void copy(Reader src, Writer dst, char[] buffer) throws IOException {
        for (int len; (len = src.read(buffer)) != -1;)
            dst.write(buffer, 0, len);
    }

    public static void copy(Reader src, Writer dst) throws IOException {
        copy(src, dst, new char[DEFAULT_BUFFER_SIZE]);
    }

    public static String readLine(InputStream is, byte[] buffer, Charset charset)
            throws IOException {
        int i = 0;
        for (int c; (c = is.read()) != -1;) {
            if (c == '\n') {
                if (i > 0 && buffer[i - 1] == '\r')
                    i--;
                if (i == 0)
                    return "";
                return new String(buffer, 0, i, charset);
            }
            buffer[i++] = (byte) c;
        }
        if (i == 0)
            return null;
        return new String(buffer, 0, i, charset);
    }

    public static String readLine(InputStream is, byte[] buffer, String charset)
            throws IOException {
        return readLine(is, buffer, getCharset(charset));
    }

    public static String readLine(InputStream is, byte[] buffer) throws IOException {
        return readLine(is, buffer, (String) null);
    }

    public static String readLine(Reader r, char[] buffer) throws IOException {
        int i = 0;
        for (int c; (c = r.read()) != -1;) {
            if (c == '\n') {
                if (i > 0 && buffer[i - 1] == '\r')
                    i--;
                if (i == 0)
                    return "";
                return new String(buffer, 0, i);
            }
            buffer[i++] = (char) c;
        }
        if (i == 0)
            return null;
        return new String(buffer, 0, i);
    }

    public static void close(Closeable c) {
        if (c != null)
            try {
                c.close();
            } catch (IOException e) {
            }
    }

    public static Charset getCharset(String charset) {
        return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
    }

    public static boolean compareCharset(String charset1, String charset2) {
        return charset1 == charset2 || charset1 != null && charset1.equals(charset2)
                || getCharset(charset1).equals(getCharset(charset2));
    }

    public static byte[] encodeCharset(char[] chars, Charset charset) {
        ByteBuffer bb = charset.encode(CharBuffer.wrap(chars));
        return Arrays.copyOfRange(bb.array(), 0, bb.limit());
    }

    public static char[] decodeCharset(byte[] bytes, Charset charset) {
        CharBuffer cb = charset.decode(ByteBuffer.wrap(bytes));
        return Arrays.copyOfRange(cb.array(), 0, cb.limit());
    }

    public static InputStream toInputStream(Reader reader, CharsetEncoder ce) {
        return new ReaderInputStream(reader, ce);
    }

    public static InputStream toInputStream(Reader reader, String charset) {
        return new ReaderInputStream(reader, getCharset(charset).newEncoder());
    }

    public static OutputStream toOutputStream(Writer writer, CharsetDecoder cd) {
        return new WriterOutputStream(writer, cd);
    }

    public static OutputStream toOutputStream(Writer writer, String charset) {
        return new WriterOutputStream(writer, getCharset(charset).newDecoder());
    }

    public static class ReaderInputStream extends InputStream {
        protected Reader reader;
        protected CharsetEncoder ce;
        protected ByteBuffer bbuf;
        protected CharBuffer cbuf;
        protected boolean eof;

        public ReaderInputStream(Reader reader, CharsetEncoder ce) {
            this.reader = reader;
            this.ce = ce;
            cbuf = CharBuffer.allocate(2048);
            bbuf = ByteBuffer.allocate((int) (cbuf.capacity() * ce.maxBytesPerChar()));
            cbuf.limit(0);
            bbuf.limit(0);
        }

        @Override
        public int read() throws IOException {
            return eof || !(bbuf.hasRemaining() || fill()) ? -1 : bbuf.get();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (eof)
                return -1;
            int size = 0;
            while (size < len) {
                int remain = bbuf.remaining();
                if (remain == 0) {
                    if (!fill())
                        break;
                    remain = bbuf.remaining();
                }
                int readLen = Math.min(remain, len - size);
                bbuf.get(b, off, readLen);
                size += readLen;
            }
            return size;
        }

        @Override
        public int available() throws IOException {
            return bbuf.remaining();
        }

        @Override
        public void reset() throws IOException {
            reader.reset();
            eof = false;
            cbuf.position(0);
            bbuf.position(0);
            cbuf.limit(0);
            bbuf.limit(0);
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        protected boolean fill() throws IOException {
            cbuf.clear();
            bbuf.clear();
            int len = reader.read(cbuf);
            if (len == -1) {
                eof = true;
                return false;
            }
            cbuf.flip();
            ce.reset();
            CoderResult cr = ce.encode(cbuf, bbuf, true);
            if (!cr.isUnderflow())
                cr.throwException();
            cr = ce.flush(bbuf);
            if (!cr.isUnderflow())
                cr.throwException();
            bbuf.flip();
            return true;
        }
    }

    public static class WriterOutputStream extends OutputStream {
        protected Writer writer;
        protected CharsetDecoder cd;
        protected ByteBuffer bbuf;
        protected CharBuffer cbuf;
        protected boolean eof;

        public WriterOutputStream(Writer writer, CharsetDecoder cd) {
            this.writer = writer;
            this.cd = cd;
            bbuf = ByteBuffer.allocate(4096);
            cbuf = CharBuffer.allocate((int) (bbuf.capacity() * cd.maxCharsPerByte()));
            bbuf.clear();
            cbuf.clear();
        }

        @Override
        public void write(int b) throws IOException {
            if (!bbuf.hasRemaining())
                flush0();
            bbuf.put((byte) b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            while (len != 0) {
                int remain = bbuf.remaining();
                if (remain == 0) {
                    flush0();
                    remain = bbuf.remaining();
                }
                int n = Math.min(len, remain);
                bbuf.put(b, off, n);
                off += n;
                len -= n;
            }
        }

        @Override
        public void flush() throws IOException {
            flush0();
            if (bbuf.position() != 0)
                throw new IOException("刷新失败，不是完整编码");
        }

        public void flush0() throws IOException {
            bbuf.flip();
            cd.reset();
            CoderResult cr = cd.decode(bbuf, cbuf, false);
            if (!cr.isUnderflow())
                cr.throwException();
            cbuf.flip();

            writer.write(cbuf.array(), cbuf.position(), cbuf.limit());

            int position = 0;
            for (int i = bbuf.position(), len = bbuf.limit(); i < len; i++, position++)
                bbuf.put(position, bbuf.get(i));

            bbuf.clear();
            cbuf.clear();

            bbuf.position(position);
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
