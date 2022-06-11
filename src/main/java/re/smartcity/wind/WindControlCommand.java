package re.smartcity.wind;

public class WindControlCommand {
    private WindControlCommands command;
    private Object value;

    public WindControlCommand(WindControlCommands command, Object value) {
        this.command = command;
        this.value = value;
    }

    public WindControlCommands getCommand() {
        return command;
    }

    public Object getValue() {
        return value;
    }

    public Boolean getValueAsBoolean() {
        return (Boolean) value;
    }

    public Integer getValueAsInt() {
        return (Integer) value;
    }

    @Override
    public boolean equals(Object obj) {
        // !!! ВНИАМЕНИЕ: проверяю только команды
        if (obj == this) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof WindControlCommand)) { return false; }
        return ((WindControlCommand) obj).command == this.command;
    }

    @Override
    public int hashCode() {
        // !!! ВНИМАНИЕ: строится только по команде
        return this.command.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.command, this.value);
    }
}
