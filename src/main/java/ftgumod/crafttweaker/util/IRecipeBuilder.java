package ftgumod.crafttweaker.util;

public interface IRecipeBuilder<T> {

	void add(T recipe);

	void remove(T recipe);

}