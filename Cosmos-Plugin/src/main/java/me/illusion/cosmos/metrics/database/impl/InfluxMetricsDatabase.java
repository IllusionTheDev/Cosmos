package me.illusion.cosmos.metrics.database.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import me.illusion.cosmos.metrics.database.MetricsDatabase;
import me.illusion.cosmos.utilities.time.Time;
import org.bukkit.configuration.ConfigurationSection;

public class InfluxMetricsDatabase implements MetricsDatabase {

    private Time updateInterval = new Time(3, TimeUnit.SECONDS);
    private WriteApiBlocking writeApiBlocking;

    @Override
    public CompletableFuture<Boolean> enable(ConfigurationSection section) {
        String url = section.getString("url");
        String token = section.getString("token");
        String org = section.getString("org");
        String bucket = section.getString("bucket");

        if (section.contains("update-interval")) {
            int time = section.getInt("update-interval.time");
            String unit = section.getString("update-interval.unit");

            updateInterval = new Time(time, TimeUnit.valueOf(unit.toUpperCase(Locale.ROOT)));
        }

        return CompletableFuture.supplyAsync(() -> {
            InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);

            writeApiBlocking = client.getWriteApiBlocking();

            return client.ping();
        });
    }

    @Override
    public Time getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public String getName() {
        return "influx";
    }

    @Override
    public CompletableFuture<Void> pushMetric(String metricId, Map<String, Integer> data) {
        Point point = Point
            .measurement(metricId)
            .time(System.currentTimeMillis(), WritePrecision.MS);

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            point.addField(entry.getKey(), entry.getValue());
        }

        return CompletableFuture.runAsync(() -> writeApiBlocking.writePoint(point));
    }
}
