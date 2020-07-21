package net.danlew.android.joda.sample;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SerializationTest {
    @SuppressWarnings("unchecked")
    private <T> T serializeObjectAndBack(T obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        byte[] bytes = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object result = ois.readObject();
        ois.close();

        return (T) result;
    }

    @Test
    public void testSerialization1() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");

        assertEquals(zone, serializeObjectAndBack(zone));
    }

    @Test
    public void testSerialization2() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("01:00");

        assertEquals(zone, serializeObjectAndBack(zone));
    }

    @Test
    public void testSerialization3() throws Exception {
        DateTime dateTime = DateTime.now();

        assertEquals(dateTime, serializeObjectAndBack(dateTime));
    }

    @Test
    public void testSerialization4() throws Exception {
        LocalDate localDate = LocalDate.now();

        assertEquals(localDate, serializeObjectAndBack(localDate));
    }
}
