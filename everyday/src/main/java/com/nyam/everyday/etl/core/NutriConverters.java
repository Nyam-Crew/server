package com.nyam.everyday.etl.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * NutriConverters
 *
 * @author : 장소희
 * @fileName : NutriConverters
 * @since : 25. 8. 12.
 * 파싱/단위변환/스케일링 유틸.
 * - 정책: 항상 per 100g 저장. nutConSrtrQua가 100ml인 경우도 100g으로 가정(assumed density=1.0).
 */

public final class NutriConverters {
    private NutriConverters() {}

    private static final Pattern QTY = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(g|ml)?", Pattern.CASE_INSENSITIVE);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal MAX_4_1 = new BigDecimal("999.9");

    /** 수치 파싱 (null/빈/비수치 → empty) */
    public static Optional<BigDecimal> parseNumber(String s) {
        if (s == null) return Optional.empty();
        String t = s.trim();
        if (t.isEmpty()) return Optional.empty();
        try {
            return Optional.of(new BigDecimal(t));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** "100g" / "85ml" → (값, 단위). 못 읽으면 empty */
    public static Optional<Quantity> parseQuantity(String s) {
        if (s == null) return Optional.empty();
        Matcher m = QTY.matcher(s.trim());
        if (!m.matches()) return Optional.empty();
        BigDecimal v = new BigDecimal(m.group(1));
        String u = m.group(2);
        UnitKind kind = ("ml".equalsIgnoreCase(u)) ? UnitKind.ML : UnitKind.G;
        return Optional.of(new Quantity(v, kind));
    }

    public enum UnitKind { G, ML }

    /** 수량 + 단위 */
    public record Quantity(BigDecimal value, UnitKind unit) {}

    /** mg → g */
    public static BigDecimal mgToG(BigDecimal mg) {
        return mg.divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);
    }

    /** 반올림(소수 1자리) */
    public static BigDecimal round1(BigDecimal v) {
        return v.setScale(1, RoundingMode.HALF_UP);
    }

    /** NUMERIC(4,1) 범위 클램프 */
    public static BigDecimal clamp4_1(BigDecimal v) {
        BigDecimal abs = v.abs();
        if (abs.compareTo(MAX_4_1) > 0) {
            return v.signum() >= 0 ? MAX_4_1 : MAX_4_1.negate();
        }
        return v;
    }

    /**
     * per Xg(or ml) 값을 per 100g로 환산.
     * - ml은 밀도 1.0 가정으로 Xml == Xg 처리.
     */
    public static BigDecimal toPer100g(BigDecimal valuePerX, Quantity base) {
        if (valuePerX == null || base == null) return BigDecimal.ZERO;
        BigDecimal grams = base.value(); // ml도 1:1로 간주
        if (grams.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal factor = ONE_HUNDRED.divide(grams, 6, RoundingMode.HALF_UP);
        return valuePerX.multiply(factor);
    }
}
