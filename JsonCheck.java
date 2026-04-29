import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoRequestDto;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class JsonCheck {
    public static void main(String[] args) throws Exception {
        String json = """
        {
          \"clientId\": \"sesion-1745955600000-a1b2c3d4\",
          \"nombreSesion\": \"Sesion offline prueba\",
          \"fechaInicio\": \"\",
          \"fechaFin\": \"\",
          \"version\": 1,
          \"ejercicios\": [
            {
              \"idEjercicio\": \"ejercicio-1745955600001-e1f2a3b4\",
              \"clientId\": \"ejercicio-1745955600001-e1f2a3b4\",
              \"catalogoEjercicioId\": \"bench-press\",
              \"nombre\": \"Press banca\",
              \"seriesPlanificadas\": 4,
              \"repeticionesPlanificadas\": 10,
              \"pesoPlanificado\": 60,
              \"alturaBanco\": \"30\",
              \"agarre\": \"Medio\"
            }
          ]
        }
        """;
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        var dto = mapper.readValue(json, PlantillaSesionEntrenamientoRequestDto.class);
        System.out.println("OK fechaInicio=" + dto.getFechaInicio() + ", fechaFin=" + dto.getFechaFin());
    }
}
