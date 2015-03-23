package nl.tudelft.ewi.devhub.server.models;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.git.models.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by jgmeligmeyling on 19/03/15.
 */
@Data
public class DiffBlameModel {

    private CommitModel newCommit;
    private CommitModel oldCommit;
    private List<CommitModel> ahead;
    private List<DiffBlameFile> files;

    @Data
    public static class DiffBlameFile {

        private String oldPath;
        private String newPath;
        private DiffModel.Type type;
        private List<DiffBlameContext> blocks;

        public Stream<DiffBlameContext> getBlockStream() {
            return blocks.parallelStream();
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DiffBlameContext {

            private List<DiffBlameLine> lines;

            public static Function<DiffContext, DiffBlameContext> transformer(final DiffModel diffModel, final BlameModel blame, final CommitModel newCommit) {
                return (context) -> {
                    Integer oldLineNumber = context.getOldStart();
                    Integer newLineNumber = context.getNewStart();
                    List<DiffBlameLine> lines = Lists.newArrayListWithExpectedSize(context.getDiffLines().size());

                    for(DiffLine line : context.getDiffLines()) {
                        if(line.isRemoved()) {
                            oldLineNumber++;
                        }
                        else if(line.isAdded()) {
                            newLineNumber++;
                        }
                        else {
                            oldLineNumber++; newLineNumber++;
                        }

                        DiffBlameLine diffBlameLine = new DiffBlameLine();
                        diffBlameLine.setContent(line.getContent());
                        diffBlameLine.setNewLineNumber(newLineNumber);
                        diffBlameLine.setOldLineNumber(oldLineNumber);

                        if(diffBlameLine.oldLineNumber != null) {
                            BlameModel.BlameBlock block = blame.getBlameBlock(diffBlameLine.oldLineNumber);
                            diffBlameLine.setSourceCommit(block.getFromCommitId());
                            diffBlameLine.setSourceLineNumber(block.getFromLineNumber(diffBlameLine.oldLineNumber));
                            diffBlameLine.setSourcePath(block.getFromFilePath());
                        }
                        else {
                            diffBlameLine.setSourceCommit(newCommit.getCommit());
                            diffBlameLine.setSourceLineNumber(diffBlameLine.newLineNumber);
                            diffBlameLine.setSourcePath(diffModel.getNewPath());
                        }

                        assert diffBlameLine.getSourceCommit() != null;
                        assert diffBlameLine.getSourceLineNumber() != null;
                        assert diffBlameLine.getSourcePath() != null;
                        lines.add(diffBlameLine);
                    }

                    return new DiffBlameContext(lines);
                };
            }

        }

        @Data
        public static class DiffBlameLine {

            private Integer oldLineNumber;
            private Integer newLineNumber;
            private String content;

            private String sourceCommit;
            private Integer sourceLineNumber;
            private String sourcePath;

            public boolean isContext() {
                return oldLineNumber != null && newLineNumber != null;
            }

            public boolean isAdded() {
                return oldLineNumber == null && newLineNumber != null;
            }

            public boolean isRemoved() {
                return oldLineNumber != null && newLineNumber == null;
            }

        }

        public static Function<DiffModel, DiffBlameFile> transformer(final BlameModelProvider blameModelProvider, final CommitModel newCommit) {
            return (diffModel) -> {
                DiffBlameFile result = new DiffBlameFile();
                BlameModel blame = blameModelProvider.getBlameModel(diffModel);
                result.setBlocks(Lists.transform(diffModel.getDiffContexts(), DiffBlameContext.transformer(diffModel, blame, newCommit)));
                result.setNewPath(diffModel.getNewPath());
                result.setOldPath(diffModel.getOldPath());
                result.setType(diffModel.getType());
                return result;
            };
        }

    }

    private static <T> int indexOf(List<T> list, Predicate<T> predicate) {
        int i = 0;
        for(T item : list) {
            if(predicate.test(item)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public DiffBlameFile.DiffBlameContext getSubContext(final CommitComment.Source source) {
        Predicate<DiffBlameFile.DiffBlameLine> SourceLineMatcher = (line) ->
            line.getSourcePath().equals(source.getSourceFilePath()) &&
            line.getSourceCommit().equals(source.getSourceCommit().getCommitId()) &&
            line.getSourceLineNumber().equals(source.getSourceLineNumber());


        DiffBlameFile.DiffBlameContext context = files
            .parallelStream()
            .flatMap(DiffBlameFile::getBlockStream)
            .filter((a) -> a.getLines().parallelStream().anyMatch(SourceLineMatcher))
            .findAny().orElse(null);

        if(context != null) {
            DiffBlameFile.DiffBlameContext copyContext = new DiffBlameFile.DiffBlameContext();

            List<DiffBlameFile.DiffBlameLine> oldLines = context.getLines();
            int index = indexOf(oldLines, SourceLineMatcher);
            int start = Math.max(index - 4, index);
            List<DiffBlameFile.DiffBlameLine> lines = oldLines.subList(start, index + 1);
        }
        return context;
    }

    public static interface BlameModelProvider {
        BlameModel getBlameModel(DiffModel diffModel);
    }

    public static DiffBlameModel transform(final BlameModelProvider blameModelProvider, final CommitModel newCommit, final CommitModel oldCommit, final DiffResponse diffResponse) {
        DiffBlameModel model = new DiffBlameModel();
        model.setAhead(diffResponse.getCommits());
        model.setNewCommit(newCommit);
        model.setOldCommit(oldCommit);
        model.setFiles(Lists.transform(diffResponse.getDiffs(), DiffBlameFile.transformer(blameModelProvider, newCommit)));
        return model;
    }

}
