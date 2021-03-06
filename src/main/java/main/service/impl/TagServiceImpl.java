package main.service.impl;

import main.api.response.TagDto;
import main.api.response.TagResponse;
import main.model.Tag;
import main.model.enums.ModerationStatus;
import main.repository.PostRepository;
import main.repository.Tag2PostRepository;
import main.repository.TagRepository;
import main.service.TagService;
import main.service.UtilityService;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final Tag2PostRepository tag2PostRepository;
    private final UtilityService utilityService;

    public TagServiceImpl(TagRepository tagRepository, PostRepository postRepository,
                          Tag2PostRepository tag2PostRepository, UtilityService utilityService) {
        this.tagRepository = tagRepository;
        this.postRepository = postRepository;
        this.tag2PostRepository = tag2PostRepository;
        this.utilityService = utilityService;
    }

    @Override
    public TagResponse getAllTags(String query) {

        TagResponse tagResponse = new TagResponse();
        List<Tag> tags;

        if (query == null) {
            tags = tagRepository.findAll();
        } else {
            tags = tagRepository.findByNameIsStartingWith(query);
        }

        List<TagDto> tagDtoList = getTagList(tags);

        tagResponse.setTags(getListTagDtoWithNormalizedWeight(tagDtoList));
        return tagResponse;
    }

    private List<TagDto> getTagList(List<Tag> tags) {
        List<TagDto> tagDtoList = new ArrayList<>();
        for (Tag tag : tags) {
            tagDtoList.add(createTagDto(tag));
        }
        return tagDtoList;
    }

    private TagDto createTagDto(Tag tag) {

        TagDto tagDto = new TagDto();
        tagDto.setName(tag.getName());
        tagDto.setWeight(getWeightTag(tag));
        return tagDto;
    }

    private double getWeightTag(Tag tag) {

        int countByTag = tag2PostRepository.countByTagId(tag.getId());
        int countAllTags = postRepository.countByIsActiveAndModerationStatusAndTimeBefore((byte) 1,
                ModerationStatus.ACCEPTED, utilityService.getTimeNow());

        return (double) countByTag / countAllTags;
    }

    private List<TagDto> getListTagDtoWithNormalizedWeight(List<TagDto> tagDtoList) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        double maxWeight = 0.0;
        if (!tagDtoList.isEmpty()) {
            maxWeight = tagDtoList.stream().max(Comparator.comparing(TagDto::getWeight)).get().getWeight();
        }

        double coefficient = 1 / maxWeight;

        tagDtoList.forEach(tagDto -> tagDto.setWeight(Double
                .parseDouble(decimalFormat.format(tagDto.getWeight() * coefficient)
                        .replace(",", "."))));

        return tagDtoList;
    }
}
