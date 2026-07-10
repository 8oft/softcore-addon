package com.softcore.addon.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class EnumArgumentType<T extends Enum<T>> implements ArgumentType<T> {
    private static final DynamicCommandExceptionType NO_SUCH_TYPE = new DynamicCommandExceptionType(value ->
            Text.literal(value + " is not a valid argument."));
    private final T[] values;

    public EnumArgumentType(T defaultValue) {
        this.values = defaultValue.getDeclaringClass().getEnumConstants();
    }

    public static <T extends Enum<T>> EnumArgumentType<T> enumArgument(T defaultValue) {
        return new EnumArgumentType<>(defaultValue);
    }

    public static <T extends Enum<T>> T getEnum(CommandContext<?> context, String name, T defaultValue) {
        return context.getArgument(name, defaultValue.getDeclaringClass());
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        return Arrays.stream(values)
                .filter(value -> value.toString().equals(argument))
                .findFirst()
                .orElseThrow(() -> NO_SUCH_TYPE.create(argument));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Arrays.stream(values).map(T::toString), builder);
    }
}
