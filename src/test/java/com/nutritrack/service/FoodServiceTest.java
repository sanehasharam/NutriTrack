package com.nutritrack.service;

import com.nutritrack.dao.FoodDao;
import com.nutritrack.model.Food;
import com.nutritrack.model.FoodCategory;
import com.nutritrack.model.ServingSize;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FoodServiceTest {
    @Test
    void appliesCustomThresholdsCategoryAndServingFilter() throws SQLException {
        RecordingFoodDao dao = new RecordingFoodDao();
        FoodService service = new FoodService(dao);

        service.searchFoods(" chicken ", FoodCategory.PROTEIN,
                new BigDecimal("25"), new BigDecimal("180"), ServingSize.ONE_CUP);

        assertEquals("chicken", dao.name);
        assertEquals("Protein", dao.category);
        assertEquals(new BigDecimal("25"), dao.minimumProtein);
        assertEquals(new BigDecimal("180"), dao.maximumCalories);
        assertEquals(ServingSize.ONE_CUP, dao.servingSize);
    }

    @Test
    void rejectsUnknownCategoriesAndNonPositiveServingWeights() {
        FoodService service = new FoodService(new RecordingFoodDao());

        assertThrows(IllegalArgumentException.class, () -> service.add("Food", "Dessert",
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> service.add("Food", "Grains",
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ZERO, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.searchFoods(null, null,
                new BigDecimal("-1"), null, null));
    }

    private static class RecordingFoodDao extends FoodDao {
        private String name;
        private String category;
        private BigDecimal minimumProtein;
        private BigDecimal maximumCalories;
        private ServingSize servingSize;

        private RecordingFoodDao() {
            super(null);
        }

        @Override
        public List<Food> filter(String name, String category, BigDecimal minimumProtein,
                                 BigDecimal maximumCalories, ServingSize servingSize) {
            this.name = name;
            this.category = category;
            this.minimumProtein = minimumProtein;
            this.maximumCalories = maximumCalories;
            this.servingSize = servingSize;
            return List.of();
        }
    }
}
