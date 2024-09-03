package com.mohistmc.banner.boot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public abstract class TypeAdapter<T> {

    public TypeAdapter() {
    }

    /**
     * Writes one JSON value (an array, object, string, number, boolean or null)
     * for {@code value}.
     *
     * @param value the Java object to write. May be null.
     */
    public abstract void write(JsonWriter out, T value) throws IOException;

    /**
     * Converts {@code value} to a JSON document and writes it to {@code out}.
     * Unlike Gson's similar {@link Gson#toJson(JsonElement, Appendable) toJson}
     * method, this write is strict. Create a {@link
     * JsonWriter#setLenient(boolean) lenient} {@code JsonWriter} and call
     * {@link #write(JsonWriter, Object)} for lenient writing.
     *
     * @param value the Java object to convert. May be null.
     * @since 2.2
     */
    public final void toJson(Writer out, T value) throws IOException {
        JsonWriter writer = new JsonWriter(out);
        write(writer, value);
    }

    /**
     * This wrapper method is used to make a type adapter null tolerant. In general, a
     * type adapter is required to handle nulls in write and read methods. Here is how this
     * is typically done:<br>
     * <pre>   {@code
     *
     * Gson gson = new GsonBuilder().registerTypeAdapter(Foo.class,
     *   new TypeAdapter<Foo>() {
     *     public Foo read(JsonReader in) throws IOException {
     *       if (in.peek() == JsonToken.NULL) {
     *         in.nextNull();
     *         return null;
     *       }
     *       // read a Foo from in and return it
     *     }
     *     public void write(JsonWriter out, Foo src) throws IOException {
     *       if (src == null) {
     *         out.nullValue();
     *         return;
     *       }
     *       // write src as JSON to out
     *     }
     *   }).create();
     * }</pre>
     * You can avoid this boilerplate handling of nulls by wrapping your type adapter with
     * this method. Here is how we will rewrite the above example:
     * <pre>   {@code
     *
     * Gson gson = new GsonBuilder().registerTypeAdapter(Foo.class,
     *   new TypeAdapter<Foo>() {
     *     public Foo read(JsonReader in) throws IOException {
     *       // read a Foo from in and return it
     *     }
     *     public void write(JsonWriter out, Foo src) throws IOException {
     *       // write src as JSON to out
     *     }
     *   }.nullSafe()).create();
     * }</pre>
     * Note that we didn't need to check for nulls in our type adapter after we used nullSafe.
     */
    public final com.google.gson.TypeAdapter<T> nullSafe() {
        return new com.google.gson.TypeAdapter<T>() {
            @Override public void write(JsonWriter out, T value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    TypeAdapter.this.write(out, value);
                }
            }
            @Override public T read(JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                }
                return TypeAdapter.this.read(reader);
            }
        };
    }

    /**
     * Converts {@code value} to a JSON document. Unlike Gson's similar {@link
     * Gson#toJson(Object) toJson} method, this write is strict. Create a {@link
     * JsonWriter#setLenient(boolean) lenient} {@code JsonWriter} and call
     * {@link #write(JsonWriter, Object)} for lenient writing.
     *
     * @throws JsonIOException wrapping {@code IOException}s thrown by {@link #write(JsonWriter, Object)}
     * @param value the Java object to convert. May be null.
     * @since 2.2
     */
    public final String toJson(T value) {
        StringWriter stringWriter = new StringWriter();
        try {
            toJson(stringWriter, value);
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
        return stringWriter.toString();
    }

    /**
     * Converts {@code value} to a JSON tree.
     *
     * @param value the Java object to convert. May be null.
     * @return the converted JSON tree. May be {@link JsonNull}.
     * @throws JsonIOException wrapping {@code IOException}s thrown by {@link #write(JsonWriter, Object)}
     * @since 2.2
     */
    public final JsonElement toJsonTree(T value) {
        try {
            JsonTreeWriter jsonWriter = new JsonTreeWriter();
            write(jsonWriter, value);
            return jsonWriter.get();
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }

    /**
     * Reads one JSON value (an array, object, string, number, boolean or null)
     * and converts it to a Java object. Returns the converted object.
     *
     * @return the converted Java object. May be null.
     */
    public abstract T read(JsonReader in) throws IOException;

    /**
     * Converts the JSON document in {@code in} to a Java object. Unlike Gson's
     * similar {@link Gson#fromJson(Reader, Class) fromJson} method, this
     * read is strict. Create a {@link JsonReader#setLenient(boolean) lenient}
     * {@code JsonReader} and call {@link #read(JsonReader)} for lenient reading.
     *
     * <p>No exception is thrown if the JSON data has multiple top-level JSON elements,
     * or if there is trailing data.
     *
     * @return the converted Java object. May be null.
     * @since 2.2
     */
    public final T fromJson(Reader in) throws IOException {
        JsonReader reader = new JsonReader(in);
        return read(reader);
    }

    /**
     * Converts the JSON document in {@code json} to a Java object. Unlike Gson's
     * similar {@link Gson#fromJson(String, Class) fromJson} method, this read is
     * strict. Create a {@link JsonReader#setLenient(boolean) lenient} {@code
     * JsonReader} and call {@link #read(JsonReader)} for lenient reading.
     *
     * <p>No exception is thrown if the JSON data has multiple top-level JSON elements,
     * or if there is trailing data.
     *
     * @return the converted Java object. May be null.
     * @since 2.2
     */
    public final T fromJson(String json) throws IOException {
        return fromJson(new StringReader(json));
    }

    /**
     * Converts {@code jsonTree} to a Java object.
     *
     * @param jsonTree the JSON element to convert. May be {@link JsonNull}.
     * @return the converted Java object. May be null.
     * @throws JsonIOException wrapping {@code IOException}s thrown by {@link #read(JsonReader)}
     * @since 2.2
     */
    public final T fromJsonTree(JsonElement jsonTree) {
        try {
            JsonReader jsonReader = new JsonTreeReader(jsonTree);
            return read(jsonReader);
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }
}
