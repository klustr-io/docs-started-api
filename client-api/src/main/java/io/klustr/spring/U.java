package io.klustr.spring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.CharMatcher;
import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.util.DigestUtils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class U {
    private static final double EARTH_RADIUS_KM = 6371;
    private static final org.joda.time.format.DateTimeFormatter yyyyMMdd = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final Random r = new SecureRandom();
    private static ObjectMapper mapper = JsonFactory.build();


    public static <T extends Enum<?>> T toEnum(Class<T> enumeration, String txt) {
        if (false == StringUtils.isNotBlank(txt)) return null;

        for (T each : enumeration.getEnumConstants()) {
            if (each.name().equalsIgnoreCase(txt)) {
                return each;
            }
        }
        return null;
    }



    public static String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
    }


    public static String encodeURIComponent(String s) {
        String result = null;

        try {
            // keep line breaks
            String txt = s.replace("\n", "<br/>");
            // remove all other control characters
            txt = CharMatcher.javaIsoControl().removeFrom(txt);
            // encode back in new lines
            txt = txt.replace("<br/>", "\n");

            result = URLEncoder.encode(txt, "UTF-8").replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!").replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(").replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        }

        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Compute the differences between the latitudes and longitudes
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Compute the Haversine formula
        double a = Math.pow(Math.sin(deltaLat / 2), 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.pow(Math.sin(deltaLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Compute the distance
        return EARTH_RADIUS_KM * c;
    }

    public static String decodeURIComponent(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }


    public static int toEpochSeconds(DateTime dt) {
        return (int) (dt.toDate().getTime() / 1000L);
    }

    private static GsonBuilder supportLocalDateTime(GsonBuilder gson) {
        return gson.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
    }

    public static ObjectMapper getObjectMapper() {
        return JsonFactory.build();
    }

    public static JsonNode toJsonNode(String json) {
        try {
            return mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toYaml(String json) throws Exception {
        // parse JSON
        JsonNode jsonNodeTree = new ObjectMapper().readTree(json);
        // save it as YAML
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }

    public static String yyyyMMdd(DateTime dt) {
        return yyyyMMdd.print(dt);
    }

    public static String trimToUpperCase(String v) {
        if (StringUtils.isNotBlank(v)) {
            return v.trim().toUpperCase();
        }
        return "";
    }

    public static String toJson(final Object obj) {
        if (obj == null) {
            return null;
        }
        // default serializer
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to json", e);
        }
    }


    public static String toJsonPrettyFormat(final Object obj) {
        if (obj == null)
            throw new IllegalArgumentException("Missing object");
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to json", e);
        }
    }


    public static <T> T fromJson(final String json, final Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to json", ex);
        }
    }

    public static <T> T fromYaml(final String yaml, final Class<T> type) {
        try {
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            return yamlReader.readValue(yaml, type);
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to json", ex);
        }
    }

    public static <T> List<T> fromYamlGenericList(final String yaml, final Class<T> type) {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, type);
        try {
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object o = yamlReader.readValue(yaml, javaType);
            return (List<T>)o;
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to json", ex);
        }
    }


    public static <T> T fromJsonParametricType(final String json, Class<?> parametrized, Class<?>... parameterClasses) {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
        try {
            return mapper.readValue(json, javaType);
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to json", ex);
        }
    }

    public static String getResourceAsString(String file, Object container) {
        try {
            return IOUtils.toString(container.getClass().getClassLoader().getResourceAsStream(file));
        } catch (Exception e) {
            throw new RuntimeException("Unable to access file " + file, e);
        }
    }

    public static InputStream getResourceAsStream(String file, Object container) {
        try {
            return container.getClass().getClassLoader().getResourceAsStream(file);
        } catch (Exception e) {
            throw new RuntimeException("Unable to access file " + file, e);
        }
    }


    public static final int randomNumber(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return r.nextInt((max - min) + 1) + min;
    }

    public static final double randomDouble(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return (double) (r.nextInt((max - min) + 1) + min);
    }

    public static String sanitizeToAlphaNumericOnly(String input) {
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }

    public static class LocalDateTypeAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public JsonElement serialize(final LocalDate date, final Type typeOfSrc,
                                     final JsonSerializationContext context) {
            return new JsonPrimitive(date.format(formatter));
        }

        @Override
        public LocalDate deserialize(final JsonElement json, final Type typeOfT,
                                     final JsonDeserializationContext context) throws JsonParseException {
            return LocalDate.parse(json.getAsString(), formatter);
        }
    }

    private static class JsonFactory {
        public static ObjectMapper build() {
            ObjectMapper o = new ObjectMapper();
            // ISO3 format for JSON documents
            o = o.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
            // serialization defaults
            o = o.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            // ignore unknown
            o = o.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // joda date time serializer
            o = o.registerModule(new JodaModule());
            // Optional.of()
            o = o.registerModule(new Jdk8Module());
            return o;
        }
    }
}
