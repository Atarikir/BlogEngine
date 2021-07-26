package main.service.impl;

import main.api.response.TagDto;
import main.api.response.TagResponse;
import main.model.Tag;
import main.repository.TagRepository;
import main.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public TagResponse getAllTags() {

        TagResponse tagResponse = new TagResponse();

        List<Tag> tags = tagRepository.findAll();
        tagResponse.setTags(tags.stream().map(this::mapToTagDto).collect(Collectors.toList()));
        return tagResponse;
    }

    public TagDto mapToTagDto(Tag tag) {
        TagDto tagDto = new TagDto();
        tagDto.setName(tag.getName());
        tagDto.setWeight(1);
        return tagDto;
    }
}
