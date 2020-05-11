package wooteco.subway.admin.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import wooteco.subway.admin.domain.Line;
import wooteco.subway.admin.domain.LineStation;
import wooteco.subway.admin.domain.Station;
import wooteco.subway.admin.dto.LineResponse;
import wooteco.subway.admin.dto.LineStationCreateRequest;
import wooteco.subway.admin.repository.LineRepository;
import wooteco.subway.admin.repository.StationRepository;

@Service
public class LineService {
    private LineRepository lineRepository;
    private StationRepository stationRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    public Line save(Line line) {
        if (lineRepository.existsByName(line.getName())) {
            throw new IllegalArgumentException("중복된 노선 이름은 등록할 수 없습니다.");
        }
        return lineRepository.save(line);
    }

    public Line showLine(Long id) {
        return lineRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    public List<LineResponse> showLines() {
        List<Line> persistLines = lineRepository.findAll();
        return persistLines.stream()
            .map(line -> findLineWithStationsById(line.getId()))
            .collect(Collectors.toList());
    }

    public Line updateLine(Long id, Line line) {
        Line persistLine = lineRepository.findById(id).orElseThrow(RuntimeException::new);
        persistLine.update(line);
        return lineRepository.save(persistLine);
    }

    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }

    public LineResponse addLineStation(Long id, LineStationCreateRequest request) {
        Line line = lineRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("노선이 존재하지 않습니다."));

        if (line.getStations().isEmpty() && request.getPreStationId() != null) {
            LineStation initialLineStation = new LineStation(null, request.getPreStationId(), 0, 0);
            line.addLineStation(initialLineStation);
        }

        line.addLineStation(request.toLineStation());
        Line persistLine = lineRepository.save(line);

        if (Objects.isNull(line.getId())) {
            return LineResponse.of(line);
        }

        return findLineWithStationsById(persistLine.getId());
    }

    public void removeLineStation(Long lineId, Long stationId) {
        Line line = lineRepository.findById(lineId)
            .orElseThrow(() -> new NoSuchElementException("노선이 존재하지 않습니다."));
        line.removeLineStationById(stationId);
        lineRepository.save(line);
    }

    public LineResponse findLineWithStationsById(Long id) {
        Line line = lineRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("노선이 존재하지 않습니다."));
        Set<Station> stations = stationRepository.findAllById(line.findLineStationsId());
        return LineResponse.of(line, stations);
    }
}
