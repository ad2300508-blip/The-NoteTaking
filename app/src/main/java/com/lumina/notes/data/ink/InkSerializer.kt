package com.lumina.notes.data.ink

import org.json.JSONArray
import org.json.JSONObject

/**
 * Compact JSON (de)serialization of ink. Points are packed into a flat
 * [x, y, pressure, x, y, pressure, …] array to keep stored notes small even
 * for dense, high-frequency S Pen sampling.
 */
object InkSerializer {

    fun encode(strokes: List<Stroke>): String {
        if (strokes.isEmpty()) return "[]"
        val arr = JSONArray()
        for (s in strokes) {
            val obj = JSONObject()
            obj.put("c", s.color)
            obj.put("w", s.baseWidth.toDouble())
            obj.put("t", s.tool.ordinal)
            if (s.tilt != 0f) obj.put("ti", s.tilt.toDouble())
            val pts = JSONArray()
            for (p in s.points) {
                pts.put(p.x.toDouble())
                pts.put(p.y.toDouble())
                pts.put(p.pressure.toDouble())
            }
            obj.put("p", pts)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun decode(json: String): List<Stroke> {
        if (json.isBlank() || json == "[]") return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            val out = ArrayList<Stroke>(arr.length())
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val pts = obj.getJSONArray("p")
                val points = ArrayList<StrokePoint>(pts.length() / 3)
                var k = 0
                while (k + 2 < pts.length()) {
                    points.add(
                        StrokePoint(
                            x = pts.getDouble(k).toFloat(),
                            y = pts.getDouble(k + 1).toFloat(),
                            pressure = pts.getDouble(k + 2).toFloat(),
                        )
                    )
                    k += 3
                }
                val toolOrdinal = obj.optInt("t", 0).coerceIn(0, PenTool.entries.lastIndex)
                out.add(
                    Stroke(
                        points = points,
                        color = obj.getLong("c"),
                        baseWidth = obj.getDouble("w").toFloat(),
                        tool = PenTool.entries[toolOrdinal],
                        tilt = obj.optDouble("ti", 0.0).toFloat(),
                    )
                )
            }
            out
        }.getOrElse { emptyList() }
    }
}
