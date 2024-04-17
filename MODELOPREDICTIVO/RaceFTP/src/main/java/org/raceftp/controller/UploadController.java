package org.raceftp.controller;

import org.raceftp.models.ProcessedTrackPoint;
import org.raceftp.models.TrackPointData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Controller
public class UploadController {

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("archivo") MultipartFile file,
                                   @RequestParam("ftp") String ftp,
                                   @RequestParam("edad") int edad,
                                   @RequestParam("porcentaje") int porcentaje,
                                   Model model) {
        try {
            // Verificar si se ha subido un archivo
            if (file.isEmpty()) {
                model.addAttribute("error", "Por favor, selecciona un archivo.");
                return "error"; // Página de error si no se selecciona un archivo
            }

            // Procesar el archivo XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(file.getBytes()));

            // Obtener la lista de trackpoints
            NodeList trackpoints = doc.getElementsByTagName("Trackpoint");

            // Lista para almacenar los datos de cada punto de seguimiento
            List<TrackPointData> trackpointDataList = new ArrayList<>();

            // Variables para calcular la media
            int totalSpeed = 0;
            int totalHeartRate = 0;
            double totalDistance = 0;
            int count = 0;

            // Variables para el seguimiento de los intervalos de tiempo
            long startTime = 0;
            long endTime = 0;
            long interval = 5 * 60 * 1000; // Intervalo de 5 minutos en milisegundos

            // Recorrer todos los trackpoints
            for (int i = 0; i < trackpoints.getLength(); i++) {
                Node node = trackpoints.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    // Obtener los datos de cada punto de seguimiento

                    Node timeNode = element.getElementsByTagName("Time").item(0);
                    String time = timeNode != null ? timeNode.getTextContent() : "N/A";

                    //Node speedNode = element.getElementsByTagName("Speed").item(0);
                    double speed = getSpeedValue(element);

                    Node heartRateNode = element.getElementsByTagName("HeartRateBpm").item(0);
                    int heartRate = heartRateNode != null ? Integer.parseInt(heartRateNode.getTextContent().trim()) : 0;

                    Node distanceNode = element.getElementsByTagName("DistanceMeters").item(0);
                    double distance = distanceNode != null ? Double.parseDouble(distanceNode.getTextContent().trim()) : 0.0;


                    // Calcular la distancia total y el ritmo cardíaco promedio
                    totalSpeed += speed;
                    totalHeartRate += heartRate;
                    totalDistance = distance; // Actualizamos la distancia total con el último valor

                    // Comprobar si ha pasado el intervalo de 5 minutos
                    if (startTime == 0) {
                        startTime = parseTime(time);
                    } else {
                        endTime = parseTime(time);
                        if (endTime - startTime >= interval) {
                            // Calcular la media y añadir los datos al array
                            double avgSpeed = totalSpeed / count;
                            int avgHeartRate = totalHeartRate / count;
                            trackpointDataList.add(new TrackPointData(time, avgSpeed, avgHeartRate, totalDistance, edad, Integer.parseInt(ftp)));
                            // Reiniciar variables para el siguiente intervalo
                            totalSpeed = 0;
                            totalHeartRate = 0;
                            totalDistance = 0;
                            count = 0;
                            startTime = endTime;
                        }
                    }
                    count++;
                }
            }

            List<ProcessedTrackPoint> processedTrackPointList = processTrackPointDataList(trackpointDataList, porcentaje);
            // Agregar la lista de puntos de seguimiento procesados al modelo
            model.addAttribute("processedTrackPointList", processedTrackPointList);
            // Agregar los datos al modelo para mostrar en la vista
            model.addAttribute("trackpointDataList", trackpointDataList);
            model.addAttribute("ftpValue", ftp);
            model.addAttribute("edad", edad);
            model.addAttribute("porcentaje", porcentaje);
            return "upload-succes"; // Página de éxito para mostrar después de cargar el archivo
        } catch (IOException e) {
            // Manejar errores de entrada/salida
            model.addAttribute("error", "Hubo un error de entrada/salida al procesar el archivo.");
            e.printStackTrace();
            return "error";
        } catch (ParserConfigurationException | SAXException | NumberFormatException e) {
            // Manejar errores de configuración del parser XML, SAX o de formato de número
            model.addAttribute("error", "Hubo un error al procesar el archivo XML.");
            e.printStackTrace();
            return "error";
        } catch (Exception e) {
            // Manejar otros errores inesperados
            model.addAttribute("error", "Hubo un error inesperado al procesar el archivo.");
            e.printStackTrace();
            return "error";
        }
    }
    // Método auxiliar para obtener el valor de Speed independientemente del prefijo del namespace
    private double getSpeedValue(Element element) {
        Node speedNode = element.getElementsByTagName("Speed").item(0);
        if (speedNode == null) {
            speedNode = element.getElementsByTagNameNS("ns3", "Speed").item(0);
            if (speedNode != null) {
                // Convertir de m/s a km/h y redondear a dos decimales
                return Math.round(Double.parseDouble(speedNode.getTextContent()) * 3.6 * 100.0) / 100.0;
            }
        } else {
            // Convertir de m/s a km/h y redondear a dos decimales
            return Math.round(Double.parseDouble(speedNode.getTextContent()) * 3.6 * 100.0) / 100.0;
        }
        return 0.0; // Devolver 0.0 si no se encuentra ningún nodo de velocidad
    }



    // Método para convertir la cadena de tiempo en milisegundos
    private long parseTime(String time) {
        String[] parts = time.split("T")[1].split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2].substring(0, 2)); // Eliminar el último carácter "Z"
        return (hours * 60 * 60 + minutes * 60 + seconds) * 1000;
    }

    // Método para procesar la lista de TrackPointData y generar una lista de ProcessedTrackPoint
    private List<ProcessedTrackPoint> processTrackPointDataList(List<TrackPointData> trackpointDataList, int porcentaje) {
        List<ProcessedTrackPoint> processedTrackPointList = new ArrayList<>();

        for (int i = 0; i < trackpointDataList.size(); i++) {

            TrackPointData trackPointData = trackpointDataList.get(i);
            int maxHeartRate = 220 - trackPointData.getAge();
            int heartRate = trackPointData.getHeartRate();
            int maxWatts = trackPointData.getFtp();
            // Calcular los vatios equivalentes según el porcentaje proporcionado
            int adjustedWatts;

            if (porcentaje != 0) {
                double adjustmentFactor = 1 + (porcentaje / 100.0); // Convertir el porcentaje a factor de ajuste
                adjustedWatts = (int) (maxWatts * adjustmentFactor); // Aplicar el ajuste al FTP máximo
            } else {
                adjustedWatts = maxWatts; // Mantener los vatios como el FTP máximo si el porcentaje es 0
            }
            // Calcular los vatios equivalentes si la frecuencia cardíaca es superior al máximo
            int watts = heartRate > maxHeartRate ? (adjustedWatts * heartRate) / maxHeartRate : 0;
            // Calcular los vatios equivalentes si la frecuencia cardíaca es inferior al máximo
            watts = heartRate < maxHeartRate ? (adjustedWatts * heartRate) / maxHeartRate : watts;
            // Crear un nuevo objeto ProcessedTrackPoint con los datos procesados
            ProcessedTrackPoint processedTrackPoint = new ProcessedTrackPoint("5 minutos", heartRate, watts);

            // Si los vatios son diferentes de 0, agregar el objeto al listado
            if (watts != 0) {
                processedTrackPointList.add(processedTrackPoint);
            }

            // Calcular los vatios al 60% del FTP máximo y agregarlos entre cada posición
            if (i < trackpointDataList.size() - 1) {
                int sixtyPercentWatts = (int) (0.6 * adjustedWatts);
                // Si los vatios al 60% son diferentes de 0, agregar el objeto al listado
                if (sixtyPercentWatts != 0) {
                    processedTrackPointList.add(new ProcessedTrackPoint("3 minutos", heartRate - 20, sixtyPercentWatts));
                }
            }
        }
        return processedTrackPointList;
    }



}
